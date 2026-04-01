CREATE TYPE "public"."role" AS ENUM ('admin', 'editor', 'viewer');

--> statement-breakpoint
CREATE TABLE
	"glossary_users" (
		"glossary_id" text NOT NULL,
		"user_id" integer NOT NULL,
		"role" "role" NOT NULL
	);

--> statement-breakpoint
DROP INDEX "idx_unique_glossary";

--> statement-breakpoint
ALTER TABLE "glossaries"
ADD COLUMN "owner" integer NOT NULL DEFAULT 1;

--> statement-breakpoint
ALTER TABLE "glossary_users" ADD CONSTRAINT "glossary_users_glossary_id_glossaries_id_fk" FOREIGN KEY ("glossary_id") REFERENCES "public"."glossaries" ("id") ON DELETE cascade ON UPDATE no action;

--> statement-breakpoint
ALTER TABLE "glossary_users" ADD CONSTRAINT "glossary_users_user_id_users_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."users" ("id") ON DELETE cascade ON UPDATE no action;

--> statement-breakpoint
CREATE UNIQUE INDEX "idx_unique_glossary_user" ON "glossary_users" USING btree ("glossary_id", "user_id");

--> statement-breakpoint
ALTER TABLE "glossaries" ADD CONSTRAINT "glossaries_owner_users_id_fk" FOREIGN KEY ("owner") REFERENCES "public"."users" ("id") ON DELETE no action ON UPDATE no action;

--> statement-breakpoint
CREATE UNIQUE INDEX "idx_unique_glossary" ON "glossaries" USING btree ("code", "owner");

--> statement-breakpoint
ALTER TABLE "glossaries"
DROP COLUMN "author";