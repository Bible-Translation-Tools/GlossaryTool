CREATE TYPE "public"."review_status" AS ENUM ('pending', 'approved', 'rejected');

--> statement-breakpoint
CREATE TABLE
	"phrase_reviews" (
		"phrase_id" text NOT NULL,
		"user_id" integer NOT NULL,
		"status" "review_status" NOT NULL
	);

--> statement-breakpoint
ALTER TABLE "phrase_reviews" ADD CONSTRAINT "phrase_reviews_phrase_id_pending_phrases_id_fk" FOREIGN KEY ("phrase_id") REFERENCES "public"."pending_phrases" ("id") ON DELETE cascade ON UPDATE no action;

--> statement-breakpoint
ALTER TABLE "phrase_reviews" ADD CONSTRAINT "phrase_reviews_user_id_users_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."users" ("id") ON DELETE cascade ON UPDATE no action;

--> statement-breakpoint
CREATE UNIQUE INDEX "idx_unique_phrase_review" ON "phrase_reviews" USING btree ("phrase_id", "user_id");