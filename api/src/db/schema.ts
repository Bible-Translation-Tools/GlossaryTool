import { relations } from "drizzle-orm";
import {
  pgTable,
  text,
  integer,
  timestamp,
  uniqueIndex,
  varchar,
  serial,
  pgEnum,
} from "drizzle-orm/pg-core";

export const roleEnum = pgEnum("role", ["owner", "admin", "editor", "viewer"]);
export type RoleType = (typeof roleEnum.enumValues)[number];

export const reviewStatusEnum = pgEnum("review_status", [
  "approved",
  "rejected",
]);
export type ReviewStatusType = (typeof reviewStatusEnum.enumValues)[number];

export const usersTable = pgTable(
  "users",
  {
    id: integer("id").primaryKey().generatedAlwaysAsIdentity(),
    wacsUserId: integer("wacs_user_id").notNull(),
    username: varchar("username", { length: 255 }).notNull(),
    email: varchar("email", { length: 255 }).notNull(),
    emoji: varchar("emoji", { length: 255 }).notNull().default("😀"),
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
    createdAt: timestamp("created_at", { withTimezone: false })
      .notNull()
      .defaultNow(),
    modifiedAt: timestamp("modified_at", { withTimezone: false })
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
    sourceLanguage: text("source_language").notNull(),
    targetLanguage: text("target_language").notNull(),
    version: integer("version").notNull().default(1),
    resourceId: integer("resource_id")
      .notNull()
      .references(() => resourceTable.id),
    createdAt: timestamp("created_at", { withTimezone: false })
      .notNull()
      .defaultNow(),
    updatedAt: timestamp("updated_at", { withTimezone: false })
      .notNull()
      .defaultNow()
      .$onUpdate(() => new Date()),
  },
  (table) => [uniqueIndex("idx_unique_glossary").on(table.id, table.code)]
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
    createdAt: timestamp("created_at", { withTimezone: false })
      .notNull()
      .defaultNow(),
    updatedAt: timestamp("updated_at", { withTimezone: false })
      .notNull()
      .defaultNow()
      .$onUpdate(() => new Date()),
  },
  (table) => [
    uniqueIndex("idx_unique_phrase").on(table.phrase, table.glossaryId),
  ]
);

export const pendingPhraseTable = pgTable(
  "pending_phrases",
  {
    id: text("id").primaryKey(),
    phrase: text("phrase").notNull(),
    spelling: text("spelling").notNull(),
    description: text("description").notNull(),
    audio: text("audio").notNull(),
    userId: integer("user_id")
      .notNull()
      .references(() => usersTable.id, { onDelete: "cascade" }),
    glossaryId: text("glossary_id")
      .notNull()
      .references(() => glossaryTable.id, { onDelete: "cascade" }),
    createdAt: timestamp("created_at", { withTimezone: false })
      .notNull()
      .defaultNow(),
    updatedAt: timestamp("updated_at", { withTimezone: false })
      .notNull()
      .defaultNow()
      .$onUpdate(() => new Date()),
  },
  (table) => [
    uniqueIndex("idx_unique_pending_phrase").on(
      table.phrase,
      table.glossaryId,
      table.userId
    ),
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

export const glossaryUsers = pgTable(
  "glossary_users",
  {
    glossaryId: text("glossary_id")
      .notNull()
      .references(() => glossaryTable.id, { onDelete: "cascade" }),
    userId: integer("user_id")
      .notNull()
      .references(() => usersTable.id, { onDelete: "cascade" }),
    role: roleEnum("role").notNull(),
  },
  (table) => [
    uniqueIndex("idx_unique_glossary_user").on(table.glossaryId, table.userId),
  ]
);

export const phraseReviews = pgTable(
  "phrase_reviews",
  {
    phraseId: text("phrase_id")
      .notNull()
      .references(() => pendingPhraseTable.id, { onDelete: "cascade" }),
    userId: integer("user_id")
      .notNull()
      .references(() => usersTable.id, { onDelete: "cascade" }),
    status: reviewStatusEnum("status").notNull(),
  },
  (table) => [
    uniqueIndex("idx_unique_phrase_review").on(table.phraseId, table.userId),
  ]
);

export const userEntityRelations = relations(usersTable, ({ many }) => ({
  glossaries: many(glossaryUsers),
}));

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
    pendingPhrases: many(pendingPhraseTable),
    users: many(glossaryUsers),
  })
);

export const glossaryUsersRelations = relations(glossaryUsers, ({ one }) => ({
  glossary: one(glossaryTable, {
    fields: [glossaryUsers.glossaryId],
    references: [glossaryTable.id],
  }),
  user: one(usersTable, {
    fields: [glossaryUsers.userId],
    references: [usersTable.id],
  }),
}));

export const phraseReviewsRelations = relations(phraseReviews, ({ one }) => ({
  phrase: one(pendingPhraseTable, {
    fields: [phraseReviews.phraseId],
    references: [pendingPhraseTable.id],
  }),
  user: one(usersTable, {
    fields: [phraseReviews.userId],
    references: [usersTable.id],
  }),
}));

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

export const pendingPhraseEntityRelations = relations(
  pendingPhraseTable,
  ({ one, many }) => ({
    glossary: one(glossaryTable, {
      fields: [pendingPhraseTable.glossaryId],
      references: [glossaryTable.id],
    }),
    original: one(phraseTable, {
      fields: [pendingPhraseTable.id],
      references: [phraseTable.id],
    }),
    user: one(usersTable, {
      fields: [pendingPhraseTable.userId],
      references: [usersTable.id],
    }),
    reviews: many(phraseReviews),
  })
);

export const refEntityRelations = relations(refTable, ({ one }) => ({
  phrase: one(phraseTable, {
    fields: [refTable.phraseId],
    references: [phraseTable.id],
  }),
}));
