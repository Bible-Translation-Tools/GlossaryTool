import { relations } from "drizzle-orm";
import {
  pgTable,
  text,
  integer,
  timestamp,
  uniqueIndex,
  varchar,
  serial,
} from "drizzle-orm/pg-core";

export const usersTable = pgTable(
  "users",
  {
    id: integer("id").primaryKey().generatedAlwaysAsIdentity(),
    wacsUserId: integer("wacs_user_id").notNull(),
    username: varchar("username", { length: 255 }).notNull(),
    email: varchar("email", { length: 255 }).notNull(),
    accessToken: text("access_token"),
    refreshToken: text("refresh_token"),
    tokenType: varchar("token_type", { length: 255 }),
    state: varchar("state", { length: 255 }),
    createdAt: timestamp("created_at").defaultNow().notNull(),
    updatedAt: timestamp("updated_at").defaultNow().notNull(),
  },
  (table) => [uniqueIndex("idx_unique_user").on(table.email)]
);

export const resourceTable = pgTable(
  "resources",
  {
    id: serial("id").primaryKey(),
    language: text("language").notNull(),
    type: text("type").notNull(),
    version: text("version").notNull(),
    createdAt: timestamp("created_at", { withTimezone: true })
      .notNull()
      .defaultNow(),
    modifiedAt: timestamp("modified_at", { withTimezone: true })
      .notNull()
      .defaultNow()
      .$onUpdate(() => new Date()),
  },
  (table) => [uniqueIndex("idx_unique_resource").on(table.language, table.type)]
);

export const glossaryTable = pgTable(
  "glossaries",
  {
    id: text("id").primaryKey(),
    code: text("code").notNull(),
    author: text("author").notNull(),
    sourceLanguage: text("source_language").notNull(),
    targetLanguage: text("target_language").notNull(),
    resourceId: integer("resource_id")
      .notNull()
      .references(() => resourceTable.id),
    createdAt: timestamp("created_at", { withTimezone: true })
      .notNull()
      .defaultNow(),
    updatedAt: timestamp("updated_at", { withTimezone: true })
      .notNull()
      .defaultNow()
      .$onUpdate(() => new Date()),
  },
  (table) => [uniqueIndex("idx_unique_glossary").on(table.code, table.author)]
);

export const phraseTable = pgTable(
  "phrases",
  {
    id: text("id").primaryKey(),
    phrase: text("phrase").notNull(),
    spelling: text("spelling").notNull(),
    description: text("description").notNull(),
    audio: text("audio").notNull(),
    glossaryId: text("glossary_id")
      .notNull()
      .references(() => glossaryTable.id, { onDelete: "cascade" }),
    createdAt: timestamp("created_at", { withTimezone: true })
      .notNull()
      .defaultNow(),
    updatedAt: timestamp("updated_at", { withTimezone: true })
      .notNull()
      .defaultNow()
      .$onUpdate(() => new Date()),
  },
  (table) => [
    uniqueIndex("idx_unique_phrase").on(table.phrase, table.glossaryId),
  ]
);

export const refTable = pgTable("refs", {
  id: text("id").primaryKey(),
  book: text("book").notNull(),
  chapter: text("chapter").notNull(),
  verse: text("verse").notNull(),
  phraseId: text("phrase_id")
    .notNull()
    .references(() => phraseTable.id, { onDelete: "cascade" }),
});

export const resourceEntityRelations = relations(resourceTable, ({ many }) => ({
  glossaries: many(glossaryTable),
}));

export const glossaryEntityRelations = relations(
  glossaryTable,
  ({ one, many }) => ({
    resource: one(resourceTable, {
      fields: [glossaryTable.resourceId],
      references: [resourceTable.id],
    }),
    phrases: many(phraseTable),
  })
);

export const phraseEntityRelations = relations(
  phraseTable,
  ({ one, many }) => ({
    glossary: one(glossaryTable, {
      fields: [phraseTable.glossaryId],
      references: [glossaryTable.id],
    }),
    refs: many(refTable),
  })
);

export const refEntityRelations = relations(refTable, ({ one }) => ({
  phrase: one(phraseTable, {
    fields: [refTable.phraseId],
    references: [phraseTable.id],
  }),
}));
