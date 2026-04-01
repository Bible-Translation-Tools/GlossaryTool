-- Drop the original ROW-LEVEL 'refs' trigger
DROP TRIGGER IF EXISTS trg_ref_updated ON refs;
DROP FUNCTION IF EXISTS update_phrase_updated_at();

-- Drop the original ROW-LEVEL 'phrases' trigger
DROP TRIGGER IF EXISTS trg_phrase_updated ON phrases;
DROP FUNCTION IF EXISTS update_glossary_updated_at();

-- Drop the failed STATEMENT-LEVEL 'refs' trigger (if it was partially created)
DROP TRIGGER IF EXISTS trg_ref_updated_statement ON refs;
DROP FUNCTION IF EXISTS update_phrase_updated_at_statement();

-- Drop the "smart" 'phrases' trigger (if it was partially created)
DROP TRIGGER IF EXISTS trg_phrase_updated_smart ON phrases;
DROP FUNCTION IF EXISTS update_glossary_updated_at_smart();

-- --- Function & Trigger for INSERT ---
CREATE OR REPLACE FUNCTION update_phrase_updated_at_on_insert()
RETURNS TRIGGER AS $$
BEGIN
  UPDATE phrases
  SET updated_at = CURRENT_TIMESTAMP
  WHERE id IN (SELECT DISTINCT phrase_id FROM new_table);
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ref_inserted_statement
AFTER INSERT ON refs
REFERENCING NEW TABLE AS new_table
FOR EACH STATEMENT EXECUTE FUNCTION update_phrase_updated_at_on_insert();

-- --- Function & Trigger for DELETE ---
CREATE OR REPLACE FUNCTION update_phrase_updated_at_on_delete()
RETURNS TRIGGER AS $$
BEGIN
  UPDATE phrases
  SET updated_at = CURRENT_TIMESTAMP
  WHERE id IN (SELECT DISTINCT phrase_id FROM old_table);
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ref_deleted_statement
AFTER DELETE ON refs
REFERENCING OLD TABLE AS old_table
FOR EACH STATEMENT EXECUTE FUNCTION update_phrase_updated_at_on_delete();

-- --- Function & Trigger for UPDATE ---
CREATE OR REPLACE FUNCTION update_phrase_updated_at_on_update()
RETURNS TRIGGER AS $$
BEGIN
  UPDATE phrases
  SET updated_at = CURRENT_TIMESTAMP
  WHERE id IN (
    SELECT DISTINCT phrase_id FROM new_table
    UNION
    SELECT DISTINCT phrase_id FROM old_table
  );
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ref_updated_statement
AFTER UPDATE ON refs
REFERENCING NEW TABLE AS new_table OLD TABLE AS old_table
FOR EACH STATEMENT EXECUTE FUNCTION update_phrase_updated_at_on_update();


CREATE OR REPLACE FUNCTION update_glossary_updated_at_smart()
RETURNS TRIGGER AS $$
DECLARE
  v_glossary_id TEXT;
BEGIN
  IF (TG_OP = 'DELETE') THEN
    v_glossary_id := OLD.glossary_id;
    UPDATE glossaries SET updated_at = CURRENT_TIMESTAMP WHERE id = v_glossary_id;

  ELSIF (TG_OP = 'INSERT') THEN
    v_glossary_id := NEW.glossary_id;
    UPDATE glossaries SET updated_at = CURRENT_TIMESTAMP WHERE id = v_glossary_id;

  ELSIF (TG_OP = 'UPDATE') THEN
    v_glossary_id := NEW.glossary_id;
    
    -- This is the "smart" check that prevents 'version' from incrementing
    -- just because 'updated_at' changed.
    IF (NEW.phrase, NEW.spelling, NEW.description, NEW.audio, NEW.glossary_id) 
       IS DISTINCT FROM 
       (OLD.phrase, OLD.spelling, OLD.description, OLD.audio, OLD.glossary_id)
    THEN
      UPDATE glossaries SET updated_at = CURRENT_TIMESTAMP WHERE id = v_glossary_id;
    END IF;
    
  END IF;

  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_phrase_updated_smart
AFTER INSERT OR UPDATE OR DELETE ON phrases
FOR EACH ROW EXECUTE FUNCTION update_glossary_updated_at_smart();