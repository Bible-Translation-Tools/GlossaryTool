CREATE TABLE "glossaries" (
	"id" text PRIMARY KEY NOT NULL,
	"code" text NOT NULL,
	"author" text NOT NULL,
	"source_language" text NOT NULL,
	"target_language" text NOT NULL,
	"resource_id" integer NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "phrases" (
	"id" text PRIMARY KEY NOT NULL,
	"phrase" text NOT NULL,
	"spelling" text NOT NULL,
	"description" text NOT NULL,
	"audio" text NOT NULL,
	"glossary_id" text NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "refs" (
	"id" text PRIMARY KEY NOT NULL,
	"book" text NOT NULL,
	"chapter" text NOT NULL,
	"verse" text NOT NULL,
	"phrase_id" text NOT NULL
);
--> statement-breakpoint
CREATE TABLE "resources" (
	"id" serial PRIMARY KEY NOT NULL,
	"language" text NOT NULL,
	"type" text NOT NULL,
	"version" text NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"modified_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "users" (
	"id" integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY (sequence name "users_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START WITH 1 CACHE 1),
	"wacs_user_id" integer NOT NULL,
	"username" varchar(255) NOT NULL,
	"email" varchar(255) NOT NULL,
	"access_token" text,
	"refresh_token" text,
	"token_type" varchar(255),
	"state" varchar(255),
	"created_at" timestamp DEFAULT now() NOT NULL,
	"updated_at" timestamp DEFAULT now() NOT NULL
);
--> statement-breakpoint
ALTER TABLE "glossaries" ADD CONSTRAINT "glossaries_resource_id_resources_id_fk" FOREIGN KEY ("resource_id") REFERENCES "public"."resources"("id") ON DELETE no action ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "phrases" ADD CONSTRAINT "phrases_glossary_id_glossaries_id_fk" FOREIGN KEY ("glossary_id") REFERENCES "public"."glossaries"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "refs" ADD CONSTRAINT "refs_phrase_id_phrases_id_fk" FOREIGN KEY ("phrase_id") REFERENCES "public"."phrases"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
CREATE UNIQUE INDEX "idx_unique_glossary" ON "glossaries" USING btree ("code","author");--> statement-breakpoint
CREATE UNIQUE INDEX "idx_unique_phrase" ON "phrases" USING btree ("phrase","glossary_id");--> statement-breakpoint
CREATE UNIQUE INDEX "idx_unique_resource" ON "resources" USING btree ("language","type");--> statement-breakpoint
CREATE UNIQUE INDEX "idx_unique_user" ON "users" USING btree ("email");