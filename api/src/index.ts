import { Hono } from "hono";
import { cors } from "hono/cors";
import DbHelper from "./db/helper";
import type { JwtVariables } from "hono/jwt";
import {
  glossaryTable,
  glossaryUsers,
  pendingPhraseTable,
  phraseReviews,
  phraseTable,
  refTable,
  resourceTable,
  reviewStatusEnum,
  ReviewStatusType,
  roleEnum,
  RoleType,
  usersTable,
} from "./db/schema";
import { and, eq, gt, sql, or } from "drizzle-orm";
import { unzipSync, zipSync } from "fflate";
import { load as parseYaml } from "js-yaml";
import {
  Glossary,
  GlossaryUpdate,
  Phrase,
  PhraseReview,
  GlossaryUser,
  PendingPhrase,
} from "./glossary.types";
import { Manifest } from "./resource.types";
import { ErrorDetails, TokenRes, User, UserRes } from "./user.types";
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

    if (glossary == null || manifest == null) {
      throw new Error(
        "Could not find glossary.json or resource.zip/manifest.yml"
      );
    }

    const existentGlossary = await dbHelper
      .getDb()
      .query.glossaryTable.findFirst({
        where: eq(glossaryTable.code, glossary.code),
      });

    if (existentGlossary) {
      throw new Error("Glossary already exists.");
    }

    const insertResource = await dbHelper
      .getDb()
      .insert(resourceTable)
      .values({
        language: manifest.dublin_core.language.identifier,
        type: manifest.dublin_core.identifier,
        version: manifest.dublin_core.version,
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
        id: glossary.id,
        code: glossary.code,
        sourceLanguage: glossary.sourceLanguage,
        targetLanguage: glossary.targetLanguage,
        resourceId: resourceId,
      })
      .onConflictDoUpdate({
        target: [glossaryTable.id, glossaryTable.code],
        set: {
          sourceLanguage: glossary.sourceLanguage,
          targetLanguage: glossary.targetLanguage,
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

    const phraseValues = glossary.phrases.map((phrase) => ({
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
          code: glossary.code,
          published: true,
          user: {
            username: user.user.username,
            emoji: user.user.emoji,
          },
          role: user.role,
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

app.get("/private/api/glossary/:code/pending_phrases", async (c) => {
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
        pendingPhrases: {
          with: {
            user: true,
            original: true,
            reviews: {
              with: {
                user: true,
              },
            },
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
        "You don't have permissions to review phrases in this glossary."
      );
    }

    return c.json<PendingPhrase[]>(
      glossary.pendingPhrases.map((phrase) => {
        const pendingPhrase: Phrase = {
          id: phrase.id,
          phrase: phrase.phrase,
          spelling: phrase.spelling,
          description: phrase.description,
          audio: phrase.audio,
          createdAt: phrase.createdAt.toISOString(),
          updatedAt: phrase.updatedAt.toISOString(),
          refs: [],
        };
        const originalPhrase: Phrase | null = phrase.original
          ? {
              id: phrase.original.id,
              phrase: phrase.original.phrase,
              spelling: phrase.original.spelling,
              description: phrase.original.description,
              audio: phrase.original.audio,
              createdAt: phrase.original.createdAt.toISOString(),
              updatedAt: phrase.original.updatedAt.toISOString(),
              refs: [],
            }
          : null;

        return {
          phrase: pendingPhrase,
          user: {
            username: phrase.user.username,
            emoji: phrase.user.emoji,
          } satisfies User,
          original: originalPhrase,
          reviews: phrase.reviews.map((review) => {
            return {
              phraseId: phrase.id,
              status: review.status,
              user: {
                username: review.user.username,
                emoji: review.user.emoji,
              } satisfies User,
            };
          }),
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

app.post("/private/api/glossary/:code/pending_phrases", async (c) => {
  const dbHelper = c.get("db");
  const code = c.req.param("code");
  const auth = c.get("jwtPayload");

  const pendingPhrases = await c.req.json<Phrase[]>();

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

    const glossaryEditors = glossary.users
      .filter((user) => ["owner", "admin", "editor"].includes(user.role))
      .map((user) => user.user.username);

    if (!glossaryEditors.includes(auth.username)) {
      throw new Error(
        "You don't have permissions to upload pending phrases in this glossary."
      );
    }

    const phraseValues = pendingPhrases.map((phrase) => ({
      id: phrase.id,
      phrase: phrase.phrase,
      spelling: phrase.spelling,
      description: phrase.description,
      audio: phrase.audio,
      userId: auth.id,
      glossaryId: glossary.id,
    }));

    const CHUNK_SIZE = 1000;

    await dbHelper.getDb().transaction(async (tx) => {
      for (let i = 0; i < phraseValues.length; i += CHUNK_SIZE) {
        const chunk = phraseValues.slice(i, i + CHUNK_SIZE);
        await tx
          .insert(pendingPhraseTable)
          .values(chunk)
          .onConflictDoUpdate({
            target: [
              pendingPhraseTable.phrase,
              pendingPhraseTable.glossaryId,
              pendingPhraseTable.userId,
            ],
            set: {
              spelling: sql.raw(`excluded.spelling`),
              description: sql.raw(`excluded.description`),
              audio: sql.raw(`excluded.audio`),
            },
          });
      }
    });

    return c.json(true);
  } catch (error: any) {
    return c.json<ErrorDetails>(
      {
        error: "Failed to upload pending phrases.",
        details: error.message || "Unknown error.",
      },
      400
    );
  }
});

app.post("/private/api/glossary/:code/review_phrase", async (c) => {
  const dbHelper = c.get("db");
  const auth = c.get("jwtPayload");
  const code = c.req.param("code");
  const { phraseId, status } = await c.req.json<{
    phraseId: string;
    status: ReviewStatusType;
  }>();

  try {
    const reviewStatus = status as ReviewStatusType;

    if (!reviewStatusEnum.enumValues.includes(reviewStatus)) {
      throw new Error("Invalid review status.");
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
        "You don't have permissions to review phrases in this glossary."
      );
    }

    const phrase = await dbHelper.getDb().query.pendingPhraseTable.findFirst({
      where: and(
        eq(pendingPhraseTable.id, phraseId),
        eq(pendingPhraseTable.glossaryId, glossary.id)
      ),
    });

    if (!phrase) {
      return c.json([]);
    }

    await dbHelper
      .getDb()
      .insert(phraseReviews)
      .values({
        phraseId: phrase.id,
        userId: auth.id,
        status: reviewStatus,
      })
      .onConflictDoUpdate({
        target: [phraseReviews.phraseId, phraseReviews.userId],
        set: {
          status: reviewStatus,
        },
      });

    const updatedPhrase = await dbHelper
      .getDb()
      .query.pendingPhraseTable.findFirst({
        where: eq(pendingPhraseTable.id, phraseId),
        with: {
          reviews: {
            with: {
              user: true,
            },
          },
        },
      });

    if (!updatedPhrase) {
      return c.json([]);
    }

    const adminsCount = glossaryAdmins.length;
    const approvedReviews = updatedPhrase.reviews.filter(
      (review) => review.status === "approved"
    ).length;
    const rejectedReviews = updatedPhrase.reviews.filter(
      (review) => review.status === "rejected"
    ).length;

    // If more or equal 51% of admins approved, move to main phrases
    if (approvedReviews / adminsCount >= 0.51) {
      await dbHelper
        .getDb()
        .insert(phraseTable)
        .values({
          id: updatedPhrase.id,
          phrase: updatedPhrase.phrase,
          spelling: updatedPhrase.spelling,
          description: updatedPhrase.description,
          audio: updatedPhrase.audio,
          glossaryId: glossary.id,
        })
        .onConflictDoUpdate({
          target: [phraseTable.phrase, phraseTable.glossaryId],
          set: {
            spelling: sql.raw(`excluded.spelling`),
            description: sql.raw(`excluded.description`),
            audio: sql.raw(`excluded.audio`),
          },
        });

      // Remove from pending phrases
      await dbHelper
        .getDb()
        .delete(pendingPhraseTable)
        .where(eq(pendingPhraseTable.id, updatedPhrase.id));

      return c.json([]);
    } else if (rejectedReviews / adminsCount >= 0.51) {
      // If more than 51% of admins rejected, just remove from pending phrases
      await dbHelper
        .getDb()
        .delete(pendingPhraseTable)
        .where(eq(pendingPhraseTable.id, updatedPhrase.id));

      return c.json([]);
    } // Otherwise, keep pending phrase for more reviews

    return c.json<PhraseReview[]>(
      updatedPhrase.reviews.map((review) => {
        return {
          phraseId: updatedPhrase.id,
          status: review.status,
          user: {
            username: review.user.username,
            emoji: review.user.emoji,
          } satisfies User,
        };
      })
    );
  } catch (error: any) {
    return c.json<ErrorDetails>(
      {
        error: "Failed to update pending phrase review status.",
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
      // If glossary is not in database, return authenticated user as owner
      return c.json<GlossaryUser[]>([
        {
          code: code,
          published: false,
          role: "owner",
          user: {
            username: auth.username,
            emoji: auth.emoji,
          },
        },
      ]);
    }

    return c.json<GlossaryUser[]>(
      glossary.users.map((user) => {
        return {
          code: glossary.code,
          published: true,
          role: user.role,
          user: {
            username: user.user.username,
            emoji: user.user.emoji,
          },
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
          code: glossary.code,
          published: true,
          role: user.role,
          user: {
            username: user.user.username,
            emoji: user.user.emoji,
          },
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

export default {
  fetch: app.fetch,
  async scheduled(
    controller: ScheduledController,
    env: CloudflareBindings,
    ctx: ExecutionContext
  ) {
    try {
      // This is just to keep supabase from pausing the database automatically due to inactivity.
      const dbHelper = new DbHelper(env);
      const glossaryCount = await dbHelper
        .getDb()
        .select({ count: sql<number>`count(*)` })
        .from(glossaryTable);
      console.log(`Glossary count: ${glossaryCount[0].count}`);
    } catch (error) {
      console.error(error);
    }
  },
};
