import * as schema from "./schema";
import { drizzle } from "drizzle-orm/postgres-js";
import postgres from "postgres";
import { and, eq, exists, inArray, isNull, lte, not, sql } from "drizzle-orm";

export default class DbHelper {
  private db;

  constructor(env: CloudflareBindings) {
    const client = postgres(env.DATABASE_URL);
    this.db = drizzle(client, { schema });
  }

  getDb() {
    return this.db;
  }
}
