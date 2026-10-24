-- The points each attempt earned, and the bonus of a session finished without a mistake.
-- Attempts given before points existed keep what their first answer was worth: 1 when right,
-- 0.5 when partly right, else 0, and answers given again earn nothing.
ALTER TABLE attempt ADD COLUMN points REAL NOT NULL DEFAULT 0;
UPDATE attempt SET points = CASE verdict WHEN 'CORRECT' THEN 1 WHEN 'PARTLY' THEN 0.5 ELSE 0 END
WHERE id IN (
    SELECT id FROM (SELECT id, ROW_NUMBER() OVER (PARTITION BY subject, exercise ORDER BY at, id) AS rank FROM attempt)
    WHERE rank = 1
);
ALTER TABLE session_finished ADD COLUMN bonus REAL NOT NULL DEFAULT 0;
