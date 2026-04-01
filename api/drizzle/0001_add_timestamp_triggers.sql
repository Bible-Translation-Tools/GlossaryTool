-- Trigger 1: Update 'phrases' table when 'refs' table changes
CREATE OR REPLACE FUNCTION update_phrase_updated_at()
RETURNS TRIGGER AS $$
DECLARE
  v_phrase_id TEXT;
BEGIN
  IF (TG_OP = 'DELETE') THEN
    v_phrase_id := OLD.phrase_id;
  ELSE
    v_phrase_id := NEW.phrase_id;
  END IF;

  UPDATE phrases
  SET updated_at = CURRENT_TIMESTAMP
  WHERE id = v_phrase_id;

  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ref_updated
AFTER INSERT OR UPDATE OR DELETE ON refs
FOR EACH ROW EXECUTE FUNCTION update_phrase_updated_at();

---

-- Trigger 2: Update 'glossaries' table when 'phrases' table changes
CREATE OR REPLACE FUNCTION update_glossary_updated_at()
RETURNS TRIGGER AS $$
DECLARE
  v_glossary_id TEXT;
BEGIN
  IF (TG_OP = 'DELETE') THEN
    v_glossary_id := OLD.glossary_id;
  ELSE
    v_glossary_id := NEW.glossary_id;
  END IF;

  UPDATE glossaries
  SET updated_at = CURRENT_TIMESTAMP
  WHERE id = v_glossary_id;

  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_phrase_updated
AFTER INSERT OR UPDATE OR DELETE ON phrases
FOR EACH ROW EXECUTE FUNCTION update_glossary_updated_at();