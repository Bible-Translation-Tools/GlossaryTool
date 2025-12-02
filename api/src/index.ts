import { Hono } from "hono";
import { cors } from "hono/cors";
import DbHelper from "./db/helper";
import type { JwtVariables } from "hono/jwt";
import {
  glossaryTable,
  glossaryUsers,
  phraseTable,
  refTable,
  resourceTable,
  roleEnum,
  RoleType,
  usersTable,
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
  or,
} from "drizzle-orm";
import { unzipSync, zipSync } from "fflate";
import { load as parseYaml, dump as stringifyYaml } from "js-yaml";
import { Glossary, GlossaryUpdate } from "./glossary.types";
import { Manifest } from "./resource.types";
import { version } from "uuid";
import { ErrorDetails, GlossaryUser, TokenRes, UserRes } from "./user.types";
import { jwt, sign } from "hono/jwt";

interface AppVariables extends JwtVariables {
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

app.use("/private/api/*", async (c, next) => {
  const jwtMiddleware = jwt({
    secret: c.env.JWT_SECRET_KEY,
  });
  return jwtMiddleware(c, next);
});

app.post("/public/api/login", async (c) => {
  const dbHelper = c.get("db");
  const { username, password } = await c.req.json<{
    username: string;
    password: string;
  }>();

  try {
    const wacsUrl = `https://content.bibletranslationtools.org/api/v1/users/${username}/tokens`;
    const b64Creds = btoa(`${username}:${password}`);
    const res = await fetch(wacsUrl, {
      method: "POST",
      headers: {
        authorization: `Basic ${b64Creds}`,
        "content-type": "application/json",
      },
      body: JSON.stringify({
        name: `glossary-api-token_${Date.now()}`,
        scopes: ["write:repository", "write:user"],
      }),
    });

    if (!res.ok) {
      const resHeadersDbug: Record<string, string> = {};
      for (const pair of res.headers.entries()) {
        const key = pair[0];
        const v = pair[1];
        if (key.toLowerCase() !== "authorization") {
          resHeadersDbug[key] = v;
        }
      }
      throw new Error("Failed to get WACS api token.");
    }

    const token = (await res.json()) as TokenRes;
    const userEndpoint = `https://content.bibletranslationtools.org/api/v1/user`;
    const apiToken = token.sha1;
    const userRes = await fetch(userEndpoint, {
      headers: {
        Authorization: `token ${apiToken}`,
        Accept: "application/json",
      },
    });
    if (!userRes.ok) {
      const body = await userRes.text();
      throw new Error("Failed to get user info.");
    }
    const userData = (await userRes.json()) as UserRes;

    await dbHelper
      .getDb()
      .insert(usersTable)
      .values({
        email: userData.email,
        username: userData.username,
        wacsUserId: userData.id,
      })
      .onConflictDoUpdate({
        target: usersTable.email,
        set: {
          username: sql`EXCLUDED.username`,
          updatedAt: new Date(),
        },
      });

    const userDb = await dbHelper.getDb().query.usersTable.findFirst({
      where: eq(usersTable.username, userData.username),
    });

    if (!userDb) {
      throw new Error("User is not in the database.");
    }

    const jwtPayload = {
      id: userDb.id,
      username: userData.username,
      emoji: userDb.emoji,
      exp: Math.floor(Date.now() / 1000) + 60 * 60 * 24, // expires in 1 day
    };

    const jwtToken = await sign(jwtPayload, c.env.JWT_SECRET_KEY);

    return c.json({
      username: userData.username,
      emoji: userDb.emoji,
      token: jwtToken,
    });
  } catch (error: any) {
    return c.json<ErrorDetails>(
      {
        error: "Failed to login.",
        details: error.message || "Unknown error.",
      },
      400
    );
  }
});

// Just to verify if token is not expired
app.get("/private/api/verify", async (c) => {
  const auth = c.get("jwtPayload");
  return c.json({
    username: auth.username,
    emoji: auth.emoji,
    token: "noop",
  });
});

app.post("/private/api/glossary", async (c) => {
  const dbHelper = c.get("db");
  const auth = c.get("jwtPayload");

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
      throw new Error(
        "Could not find glossary.json or resource.zip/manifest.yml"
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
        sourceLanguage: glossary!.sourceLanguage,
        targetLanguage: glossary!.targetLanguage,
        resourceId: resourceId,
      })
      .onConflictDoUpdate({
        target: [glossaryTable.id, glossaryTable.code],
        set: {
          sourceLanguage: glossary!.sourceLanguage,
          targetLanguage: glossary!.targetLanguage,
          resourceId: resourceId,
        },
      })
      .returning({ id: glossaryTable.id });

    const glossaryId = insertGlossary[0].id;

    await dbHelper
      .getDb()
      .insert(glossaryUsers)
      .values({
        glossaryId: glossaryId,
        userId: auth.id,
        role: "owner",
      })
      .onConflictDoUpdate({
        target: [glossaryUsers.glossaryId, glossaryUsers.userId],
        set: {
          role: "owner",
        },
      });

    const phraseValues = glossary!.phrases.map((phrase) => ({
      id: phrase.id,
      phrase: phrase.phrase,
      spelling: phrase.spelling,
      description: phrase.description,
      audio: phrase.audio,
      glossaryId: glossaryId,
    }));

    const CHUNK_SIZE = 1000;

    await dbHelper.getDb().transaction(async (tx) => {
      for (let i = 0; i < phraseValues.length; i += CHUNK_SIZE) {
        const chunk = phraseValues.slice(i, i + CHUNK_SIZE);
        await tx
          .insert(phraseTable)
          .values(chunk)
          .onConflictDoUpdate({
            target: [phraseTable.phrase, phraseTable.glossaryId],
            set: {
              spelling: sql.raw(`excluded.spelling`),
              description: sql.raw(`excluded.description`),
              audio: sql.raw(`excluded.audio`),
            },
          });
      }
    });

    const updated = await dbHelper.getDb().query.glossaryTable.findFirst({
      where: eq(glossaryTable.id, glossaryId),
    });

    if (!updated) {
      throw new Error("Could not find glossary.");
    }

    return c.json(updated.version);
  } catch (error: any) {
    return c.json<ErrorDetails>(
      {
        error: "Failed to process glossary file.",
        details: error.message || "Unknown error.",
      },
      400
    );
  }
});

app.post("/private/api/glossary/:code/role", async (c) => {
  const dbHelper = c.get("db");
  const auth = c.get("jwtPayload");
  const code = c.req.param("code");
  const { username, role } = await c.req.json<{
    username: string;
    role: RoleType;
  }>();

  try {
    const userRole = role as RoleType;

    if (!roleEnum.enumValues.includes(userRole) || userRole === "owner") {
      throw new Error("Invalid role.");
    }

    const glossary = await dbHelper.getDb().query.glossaryTable.findFirst({
      where: eq(glossaryTable.code, code),
      with: {
        users: {
          with: {
            user: true,
          },
        },
      },
    });

    if (!glossary) {
      throw new Error("Invalid glossary.");
    }

    const glossaryAdmins = glossary.users
      .filter((user) => ["owner", "admin"].includes(user.role))
      .map((user) => user.user.username);

    if (!glossaryAdmins.includes(auth.username)) {
      throw new Error(
        "You don't have permissions to assign roles to this glossary."
      );
    }

    const user = await dbHelper.getDb().query.usersTable.findFirst({
      where: eq(usersTable.username, username),
    });

    if (!user) {
      throw new Error("Invalid user.");
    }

    await dbHelper
      .getDb()
      .insert(glossaryUsers)
      .values({
        glossaryId: glossary.id,
        userId: user.id,
        role: userRole,
      })
      .onConflictDoUpdate({
        target: [glossaryUsers.glossaryId, glossaryUsers.userId],
        set: {
          role: userRole,
        },
      });

    const updatedGlossary = await dbHelper
      .getDb()
      .query.glossaryTable.findFirst({
        where: eq(glossaryTable.code, code),
        with: {
          users: {
            with: {
              user: true,
            },
          },
        },
      });

    if (!updatedGlossary) {
      throw new Error("Invalid glossary.");
    }

    return c.json<GlossaryUser[]>(
      updatedGlossary.users.map((user) => {
        return {
          username: user.user.username,
          emoji: user.user.emoji,
          role: user.role,
          code: glossary.code,
        };
      })
    );
  } catch (error: any) {
    return c.json<ErrorDetails>(
      {
        error: "Failed to assign role.",
        details: error.message || "Unknown error.",
      },
      400
    );
  }
});

app.get("/public/api/glossary/:code", async (c) => {
  const dbHelper = c.get("db");
  const code = c.req.param("code");

  try {
    const encoder = new TextEncoder();

    const glossary =
      (await dbHelper.getDb().query.glossaryTable.findFirst({
        where: eq(glossaryTable.code, code),
        with: {
          resource: true,
          phrases: true,
        },
      })) || null;

    if (!glossary) {
      throw new Error("Code doesn't exist.");
    }

    const resourceFilename = `${glossary!.resource.language}_${
      glossary!.resource.type
    }.zip`;

    const resourceFile = await c.env.R2_BUCKET.get(resourceFilename);
    if (resourceFile === null) {
      throw new Error(
        `Resource file "${resourceFilename}" not found in R2 bucket.`
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
  } catch (error: any) {
    return c.json<ErrorDetails>(
      {
        error: "Failed to process raw zip file.",
        details: error.message || "Unknown error.",
      },
      400
    );
  }
});

app.get("/private/api/glossary/:code/users", async (c) => {
  const dbHelper = c.get("db");
  const code = c.req.param("code");
  const auth = c.get("jwtPayload");

  try {
    const glossary = await dbHelper.getDb().query.glossaryTable.findFirst({
      where: eq(glossaryTable.code, code),
      with: {
        users: {
          with: {
            user: true,
          },
        },
      },
    });

    if (!glossary) {
      // If glossary is not in database, return current user as owner
      return c.json<GlossaryUser[]>([
        {
          username: auth.username,
          emoji: auth.emoji,
          role: "owner",
          code: code,
        },
      ]);
    }

    const glossaryAdmins = glossary.users
      .filter((user) => ["owner", "admin"].includes(user.role))
      .map((user) => user.user.username);

    if (glossaryAdmins.includes(auth.username)) {
      return c.json<GlossaryUser[]>(
        glossary.users.map((user) => {
          return {
            username: user.user.username,
            emoji: user.user.emoji,
            role: user.role,
            code: glossary.code,
          };
        })
      );
    } else {
      return c.json([]);
    }
  } catch (error: any) {
    return c.json<ErrorDetails>(
      {
        error: "Failed to get glossary users.",
        details: error.message || "Unknown error.",
      },
      400
    );
  }
});

app.get("/private/api/glossary/:code/join", async (c) => {
  const dbHelper = c.get("db");
  const code = c.req.param("code");
  const auth = c.get("jwtPayload");

  try {
    const glossary = await dbHelper.getDb().query.glossaryTable.findFirst({
      where: eq(glossaryTable.code, code),
      with: {
        users: {
          with: {
            user: true,
          },
        },
      },
    });

    if (!glossary) {
      throw new Error("Invalid glossary.");
    }

    const users = glossary.users.map((user) => user.user.username);

    if (!users.includes(auth.username)) {
      await dbHelper
        .getDb()
        .insert(glossaryUsers)
        .values({
          glossaryId: glossary.id,
          userId: auth.id,
          role: "viewer",
        })
        .onConflictDoUpdate({
          target: [glossaryUsers.glossaryId, glossaryUsers.userId],
          set: {
            role: "viewer",
          },
        });
    }

    const updatedGlossary = await dbHelper
      .getDb()
      .query.glossaryTable.findFirst({
        where: eq(glossaryTable.code, code),
        with: {
          users: {
            with: {
              user: true,
            },
          },
        },
      });

    if (!updatedGlossary) {
      throw new Error("Invalid glossary.");
    }

    return c.json<GlossaryUser[]>(
      updatedGlossary.users.map((user) => {
        return {
          username: user.user.username,
          emoji: user.user.emoji,
          role: user.role,
          code: glossary.code,
        };
      })
    );
  } catch (error: any) {
    return c.json<ErrorDetails>(
      {
        error: "Failed to get glossary users.",
        details: error.message || "Unknown error.",
      },
      400
    );
  }
});

app.post("/public/api/glossary/check_updates", async (c) => {
  const dbHelper = c.get("db");
  const glossaries = await c.req.json<GlossaryUpdate[]>();

  if (glossaries.length === 0) return c.json([], 200);

  const conditions = glossaries.map((item) =>
    and(eq(glossaryTable.id, item.id), gt(glossaryTable.version, item.version))
  );
  const combinedQuery = or(...conditions);

  try {
    const dbResult = await dbHelper
      .getDb()
      .select({
        id: glossaryTable.id,
        code: glossaryTable.code,
        version: glossaryTable.version,
        createdAt: glossaryTable.createdAt,
        updatedAt: glossaryTable.updatedAt,
      })
      .from(glossaryTable)
      .where(combinedQuery);

    const updates: GlossaryUpdate[] = dbResult.map((item) => ({
      ...item,
      createdAt: item.createdAt.getTime(),
      updatedAt: item.updatedAt.getTime(),
    }));

    return c.json(updates);
  } catch (error: any) {
    return c.json<ErrorDetails>(
      {
        error: "Failed to check for updates.",
        details: error.message || "Unknown error.",
      },
      400
    );
  }
});

/// OLD API

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

    glossary["author"] = "User";

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
        sourceLanguage: glossary!.sourceLanguage,
        targetLanguage: glossary!.targetLanguage,
        resourceId: resourceId,
      })
      .onConflictDoUpdate({
        target: [glossaryTable.code, glossaryTable.id],
        set: {
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

export default app;
