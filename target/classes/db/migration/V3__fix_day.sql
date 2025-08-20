-- Converter SMALLINT -> INTEGER para alinhar com o Hibernate
ALTER TABLE working_hours
  ALTER COLUMN day_of_week TYPE INTEGER USING day_of_week::integer;
