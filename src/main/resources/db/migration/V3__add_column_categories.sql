ALTER TABLE video
ADD COLUMN categories jsonb NOT NULL DEFAULT '["AI"]'::jsonb;

ALTER TABLE video
ALTER COLUMN categories DROP DEFAULT;