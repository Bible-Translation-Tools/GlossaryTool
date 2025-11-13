DROP TRIGGER IF EXISTS trg_glossary_version_increment ON glossaries;
DROP FUNCTION IF EXISTS increment_glossary_version();

-- Create the new, "smarter" function
CREATE OR REPLACE FUNCTION increment_glossary_version_smart()
RETURNS TRIGGER AS $$
BEGIN
  -- Check if the version number was explicitly changed by the UPDATE statement.
  -- (e.g., "UPDATE ... SET version = 1")
  IF NEW.version IS DISTINCT FROM OLD.version THEN
    --
    -- YES, it was. Respect the manual change and do not increment.
    --
    RETURN NEW;
  ELSE
    --
    -- NO, it was not. This was an implicit update (e.g., a phrase changed).
    -- Increment the version as normal.
    --
    NEW.version := OLD.version + 1;
    RETURN NEW;
  END IF;
END;
$$ LANGUAGE plpgsql;

-- Re-create the trigger to use the new "smart" function
CREATE TRIGGER trg_glossary_version_increment
BEFORE UPDATE ON glossaries
FOR EACH ROW EXECUTE FUNCTION increment_glossary_version_smart();