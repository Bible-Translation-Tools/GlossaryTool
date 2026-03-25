ALTER TYPE "public"."review_status" ADD VALUE 'unreviewed' BEFORE 'approved';

COMMIT;

BEGIN;

--> statement-breakpoint
DROP TABLE "refs" CASCADE;

--> statement-breakpoint
ALTER TABLE "pending_phrases"
ADD COLUMN "status" "review_status" DEFAULT 'unreviewed' NOT NULL;