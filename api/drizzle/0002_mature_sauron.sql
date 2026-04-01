ALTER TABLE "glossaries" ADD COLUMN "version" integer DEFAULT 1 NOT NULL;

CREATE OR REPLACE FUNCTION increment_glossary_version()
RETURNS TRIGGER AS $$
BEGIN
  -- Set the new row's version to be the old row's version + 1
  NEW.version := OLD.version + 1;
  
  -- Return the modified new row to be inserted
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop the trigger if it already exists (makes the script re-runnable)
DROP TRIGGER IF EXISTS trg_glossary_version_increment ON glossaries;

-- Create the trigger to run BEFORE any update on the glossaries table
CREATE TRIGGER trg_glossary_version_increment
BEFORE UPDATE ON glossaries
FOR EACH ROW EXECUTE FUNCTION increment_glossary_version();