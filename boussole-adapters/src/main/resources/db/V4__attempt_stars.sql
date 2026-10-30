-- The stars each attempt earned, out of 3: the share of what the answer could earn. Attempts given
-- before stars existed take those of their verdict: 3 when right, 2 when partly right, else 0.
ALTER TABLE attempt ADD COLUMN stars INTEGER NOT NULL DEFAULT 0;
UPDATE attempt SET stars = CASE verdict WHEN 'CORRECT' THEN 3 WHEN 'PARTLY' THEN 2 ELSE 0 END;
