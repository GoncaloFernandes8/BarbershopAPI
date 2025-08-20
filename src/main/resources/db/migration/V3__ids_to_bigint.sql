-- Ajusta PKs e FKs para BIGINT (compatível com Java Long)

-- 1) Remover FKs dependentes e a EXCLUDE constraint (usa barber_id)
ALTER TABLE appointment DROP CONSTRAINT IF EXISTS appointment_barber_id_fkey;
ALTER TABLE appointment DROP CONSTRAINT IF EXISTS appointment_service_id_fkey;
ALTER TABLE appointment DROP CONSTRAINT IF EXISTS appointment_client_id_fkey;
ALTER TABLE working_hours DROP CONSTRAINT IF EXISTS working_hours_barber_id_fkey;
ALTER TABLE time_off      DROP CONSTRAINT IF EXISTS time_off_barber_id_fkey;

ALTER TABLE appointment DROP CONSTRAINT IF EXISTS appointment_no_overlap;

-- 2) PKs -> BIGINT
ALTER TABLE barber         ALTER COLUMN id TYPE BIGINT;
ALTER TABLE service        ALTER COLUMN id TYPE BIGINT;
ALTER TABLE client         ALTER COLUMN id TYPE BIGINT;
ALTER TABLE working_hours  ALTER COLUMN id TYPE BIGINT;
ALTER TABLE time_off       ALTER COLUMN id TYPE BIGINT;

-- 3) FKs -> BIGINT
ALTER TABLE working_hours  ALTER COLUMN barber_id TYPE BIGINT;
ALTER TABLE time_off       ALTER COLUMN barber_id TYPE BIGINT;
ALTER TABLE appointment    ALTER COLUMN barber_id TYPE BIGINT;
ALTER TABLE appointment    ALTER COLUMN service_id TYPE BIGINT;
ALTER TABLE appointment    ALTER COLUMN client_id TYPE BIGINT;

-- 4) Recriar FKs
ALTER TABLE working_hours
  ADD CONSTRAINT working_hours_barber_id_fkey
  FOREIGN KEY (barber_id) REFERENCES barber(id) ON DELETE CASCADE;

ALTER TABLE time_off
  ADD CONSTRAINT time_off_barber_id_fkey
  FOREIGN KEY (barber_id) REFERENCES barber(id) ON DELETE CASCADE;

ALTER TABLE appointment
  ADD CONSTRAINT appointment_barber_id_fkey
  FOREIGN KEY (barber_id) REFERENCES barber(id);

ALTER TABLE appointment
  ADD CONSTRAINT appointment_service_id_fkey
  FOREIGN KEY (service_id) REFERENCES service(id);

ALTER TABLE appointment
  ADD CONSTRAINT appointment_client_id_fkey
  FOREIGN KEY (client_id) REFERENCES client(id);

-- 5) Recriar a EXCLUDE constraint (anti-sobreposição)
ALTER TABLE appointment
  ADD CONSTRAINT appointment_no_overlap
  EXCLUDE USING gist (
    barber_id WITH =,
    tstzrange(starts_at, ends_at, '[)') WITH &&,
    is_active WITH =
  );
