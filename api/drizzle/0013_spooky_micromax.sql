ALTER TABLE "phrase_reviews" ALTER COLUMN "status" SET DATA TYPE text;--> statement-breakpoint
DROP TYPE "public"."review_status";--> statement-breakpoint
CREATE TYPE "public"."review_status" AS ENUM('approved', 'rejected');--> statement-breakpoint
ALTER TABLE "phrase_reviews" ALTER COLUMN "status" SET DATA TYPE "public"."review_status" USING "status"::"public"."review_status";