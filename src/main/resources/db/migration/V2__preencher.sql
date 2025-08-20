INSERT INTO barber (name) VALUES ('João'), ('Miguel');

INSERT INTO service (name, duration_min, buffer_after_min, price_cents)
VALUES ('Corte', 30, 0, 1200), ('Barba', 20, 0, 800);

INSERT INTO client (name, phone) VALUES ('Cliente Demo','912345678');

INSERT INTO working_hours (barber_id, day_of_week, start_time, end_time)
SELECT b, d, '09:00', '18:00'
FROM (VALUES (1),(2)) AS barbers(b)
CROSS JOIN generate_series(1,5) AS d;
