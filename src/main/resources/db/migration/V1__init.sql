CREATE TABLE customer (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  phone TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE appointment (
  id SERIAL PRIMARY KEY,
  customer_id INT NOT NULL REFERENCES customer(id),
  service TEXT NOT NULL,
  starts_at TIMESTAMPTZ NOT NULL,
  ends_at TIMESTAMPTZ NOT NULL,
  status TEXT DEFAULT 'SCHEDULED'
);