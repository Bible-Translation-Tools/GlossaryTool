DROP INDEX "idx_unique_pending_phrase";--> statement-breakpoint
CREATE UNIQUE INDEX "idx_unique_pending_phrase" ON "pending_phrases" USING btree ("phrase","glossary_id","user_id");