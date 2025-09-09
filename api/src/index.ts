import { Hono } from "hono";
import { cors } from "hono/cors";
import DbHelper from "./db/helper";
import { glossaryTable } from "./db/schema";
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

app.post("/api/glossary/:code", async (c) => {
  const dbHelper = c.get("db");
  const code = c.req.param("code");
});

app.get("/api/glossary/:code", async (c) => {
  const dbHelper = c.get("db");
  const code = c.req.param("code");

  const glossary = await dbHelper.getDb().query.glossaryTable.findMany({
    where: eq(glossaryTable.code, code),
    with: {
      resource: true,
      sourceLanguage: true,
      targetLanguage: true,
      phrases: true,
    },
  });

  return c.json(glossary);
});

export default app;
