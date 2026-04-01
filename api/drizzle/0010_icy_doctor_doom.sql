CREATE TABLE "pending_phrases" (
	"id" text PRIMARY KEY NOT NULL,
	"phrase" text NOT NULL,
	"spelling" text NOT NULL,
	"description" text NOT NULL,
	"audio" text NOT NULL,
	"user_id" integer NOT NULL,
	"glossary_id" text NOT NULL,
	"created_at" timestamp DEFAULT now() NOT NULL,
	"updated_at" timestamp DEFAULT now() NOT NULL
);
--> statement-breakpoint
ALTER TABLE "pending_phrases" ADD CONSTRAINT "pending_phrases_user_id_users_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "pending_phrases" ADD CONSTRAINT "pending_phrases_glossary_id_glossaries_id_fk" FOREIGN KEY ("glossary_id") REFERENCES "public"."glossaries"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
CREATE UNIQUE INDEX "idx_unique_pending_phrase" ON "pending_phrases" USING btree ("phrase","glossary_id");