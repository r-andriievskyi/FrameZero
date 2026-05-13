ALTER TABLE production_members
    ADD COLUMN reports_to_member_id UUID REFERENCES production_members(id);

CREATE INDEX IF NOT EXISTS idx_members_reports_to ON production_members(reports_to_member_id);
