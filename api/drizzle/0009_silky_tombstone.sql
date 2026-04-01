ALTER TYPE "public"."role" ADD VALUE 'owner' BEFORE 'admin';--> statement-breakpoint
ALTER TABLE "glossaries" DROP CONSTRAINT "glossaries_owner_users_id_fk";
--> statement-breakpoint
DROP INDEX "idx_unique_glossary";--> statement-breakpoint
CREATE UNIQUE INDEX "idx_unique_glossary" ON "glossaries" USING btree ("id","code");--> statement-breakpoint
ALTER TABLE "glossaries" DROP COLUMN "owner";