import { Hono } from "hono";
import { cors } from "hono/cors";
import DbHelper from "./db/helper";
import {
  glossaryTable,
  phraseTable,
  refTable,
  resourceTable,
} from "./db/schema";
import {
  and,
  eq,
  exists,
  gt,
  sql,
  asc,
  count,
  inArray,
  max,
  min,
} from "drizzle-orm";
import { unzipSync, zipSync } from "fflate";
import { load as parseYaml, dump as stringifyYaml } from "js-yaml";
import { Glossary } from "./glossary.types";
import { Manifest } from "./resource.types";

interface AppVariables {
  db: DbHelper;
}

const app = new Hono<{
  Bindings: CloudflareBindings;
  Variables: AppVariables;
}>();

app.use("*", cors());

app.use("*", async (c, next) => {
  const dbHelper = new DbHelper(c.env);
  c.set("db", dbHelper);
  await next();
});

// app.use("/api/*", async (c, next) => {
//   const jwtMiddleware = jwt({
//     secret: c.env.JWT_SECRET_KEY,
//   });
//   return jwtMiddleware(c, next);
// });

app.post("/api/glossary", async (c) => {
  const dbHelper = c.get("db");

  try {
    const zipArrayBuffer = await c.req.arrayBuffer();

    if (!zipArrayBuffer || zipArrayBuffer.byteLength === 0) {
      return c.json({ error: "Request body is empty." }, 400);
    }

    const decoder = new TextDecoder();

    let glossary: Glossary | null = null;
    let manifest: Manifest | null = null;

    const mainArchive = unzipSync(new Uint8Array(zipArrayBuffer));

    const glossaryFile = mainArchive["glossary.json"];
    if (glossaryFile) {
      const glossaryString = decoder.decode(glossaryFile);
      glossary = JSON.parse(glossaryString);
    }

    const resourceZipFilename = Object.keys(mainArchive).find((name) =>
      name.endsWith(".zip")
    );
    if (resourceZipFilename) {
      const resourceZipFile = mainArchive[resourceZipFilename];
      const resourceArchive = unzipSync(resourceZipFile);

      const manifestPath = Object.keys(resourceArchive).find((path) =>
        path.endsWith("manifest.yaml")
      );
      if (manifestPath) {
        const manifestFile = resourceArchive[manifestPath];
        if (manifestFile) {
          const manifestString = decoder.decode(manifestFile);
          manifest = parseYaml(manifestString) as Manifest;
        }
      }

      await c.env.R2_BUCKET.put(resourceZipFilename, resourceZipFile, {
        httpMetadata: {
          contentType: "application/zip",
        },
      });
    }

    if (glossary == null && manifest == null) {
      return c.json(
        { error: "Could not find glossary.json or resource.zip/manifest.yml" },
        404
      );
    }

    const insertResource = await dbHelper
      .getDb()
      .insert(resourceTable)
      .values({
        language: manifest!.dublin_core.language.identifier,
        type: manifest!.dublin_core.identifier,
        version: manifest!.dublin_core.version,
      })
      .onConflictDoUpdate({
        target: [resourceTable.language, resourceTable.type],
        set: {
          version: manifest!.dublin_core.version,
        },
      })
      .returning({ id: resourceTable.id });
    const resourceId = insertResource[0].id;

    const insertGlossary = await dbHelper
      .getDb()
      .insert(glossaryTable)
      .values({
        id: glossary!.id,
        code: glossary!.code,
        author: glossary!.author,
        sourceLanguage: glossary!.sourceLanguage,
        targetLanguage: glossary!.targetLanguage,
        resourceId: resourceId,
      })
      .onConflictDoUpdate({
        target: [glossaryTable.code, glossaryTable.author],
        set: {
          author: glossary!.author,
          sourceLanguage: glossary!.sourceLanguage,
          targetLanguage: glossary!.targetLanguage,
          resourceId: resourceId,
        },
      })
      .returning({ id: glossaryTable.id });
    const glossaryId = insertGlossary[0].id;

    for (const phrase of glossary!.phrases) {
      const insertPhrase = await dbHelper
        .getDb()
        .insert(phraseTable)
        .values({
          id: phrase.id,
          phrase: phrase.phrase,
          spelling: phrase.spelling,
          description: phrase.description,
          audio: phrase.audio,
          glossaryId: glossaryId,
        })
        .onConflictDoUpdate({
          target: [phraseTable.phrase, phraseTable.glossaryId],
          set: {
            spelling: phrase.spelling,
            description: phrase.description,
            audio: phrase.audio,
          },
        })
        .returning({ id: phraseTable.id });

      const phraseId = insertPhrase[0].id;

      await dbHelper
        .getDb()
        .insert(refTable)
        .values(phrase.refs.map((ref) => ({ ...ref, phraseId: phraseId })))
        .onConflictDoNothing({
          target: [refTable.id],
        });
    }

    return c.json(true);
  } catch (error) {
    return c.json(
      { error: "Failed to process raw zip file.", details: error },
      500
    );
  }
});

app.get("/api/glossary/:code", async (c) => {
  const dbHelper = c.get("db");
  const code = c.req.param("code");

  try {
    const encoder = new TextEncoder();

    const glossary =
      (await dbHelper.getDb().query.glossaryTable.findFirst({
        where: eq(glossaryTable.code, code),
        with: {
          resource: true,
          phrases: {
            with: {
              refs: true,
            },
          },
        },
      })) || null;

    if (!glossary) {
      return c.json({ error: "Could not find glossary" }, 404);
    }

    const resourceFilename = `${glossary!.resource.language}_${
      glossary!.resource.type
    }.zip`;

    const resourceFile = await c.env.R2_BUCKET.get(resourceFilename);
    if (resourceFile === null) {
      return c.json(
        { error: `Resource file "${resourceFilename}" not found in R2.` },
        404
      );
    }
    const resourceBytes = await resourceFile.arrayBuffer();
    const glossaryJson = JSON.stringify(glossary, null, 4);
    const mainZipContents = {
      "glossary.json": encoder.encode(glossaryJson),
      [resourceFilename]: new Uint8Array(resourceBytes),
    };
    const mainZipBytes = zipSync(mainZipContents);
    const filename = `glossary-${code}.zip`;

    return new Response(mainZipBytes, {
      headers: {
        "Content-Type": "application/zip",
        "Content-Disposition": `attachment; filename="${filename}"`,
      },
    });
  } catch (error) {
    return c.json(
      { error: "Failed to process raw zip file.", details: error },
      500
    );
  }
});

export default app;
