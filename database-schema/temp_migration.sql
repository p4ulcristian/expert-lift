-- Drop the existing status column and recreate it as UUID
ALTER TABLE "public"."orders" DROP COLUMN "status";
ALTER TABLE "public"."orders" ADD COLUMN "status" uuid NULL; 