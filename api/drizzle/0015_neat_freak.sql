DROP INDEX "idx_unique_glossary";--> statement-breakpoint
CREATE UNIQUE INDEX "idx_unique_glossary" ON "glossaries" USING btree ("code","source_language","target_language");