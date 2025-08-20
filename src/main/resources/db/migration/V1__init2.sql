CREATE EXTENSION IF NOT EXISTS btree_gist;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE barber (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE service (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  duration_min INT NOT NULL,
  buffer_after_min INT NOT NULL DEFAULT 0,
  price_cents INT,
  is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE client (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  phone TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE working_hours (
  id SERIAL PRIMARY KEY,
  barber_id INT NOT NULL REFERENCES barber(id) ON DELETE CASCADE,
  day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  UNIQUE (barber_id, day_of_week, start_time, end_time),
  CHECK (start_time < end_time)
);

CREATE TABLE time_off (
  id SERIAL PRIMARY KEY,
  barber_id INT NOT NULL REFERENCES barber(id) ON DELETE CASCADE,
  starts_at TIMESTAMPTZ NOT NULL,
  ends_at TIMESTAMPTZ NOT NULL,
  reason TEXT,
  CHECK (starts_at < ends_at)
);

CREATE TABLE appointment (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  barber_id INT NOT NULL REFERENCES barber(id),
  service_id INT NOT NULL REFERENCES service(id),
  client_id INT NOT NULL REFERENCES client(id),
  starts_at TIMESTAMPTZ NOT NULL,
  ends_at TIMESTAMPTZ NOT NULL,
  status TEXT NOT NULL DEFAULT 'SCHEDULED',
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (starts_at < ends_at)
);

ALTER TABLE appointment
  ADD CONSTRAINT appointment_no_overlap
  EXCLUDE USING gist (
    barber_id WITH =,
    tstzrange(starts_at, ends_at, '[)') WITH &&,
    is_active WITH =
  );

CREATE INDEX IF NOT EXISTS idx_appointment_barber_start ON appointment (barber_id, starts_at);
CREATE INDEX IF NOT EXISTS idx_timeoff_barber ON time_off (barber_id, starts_at);
