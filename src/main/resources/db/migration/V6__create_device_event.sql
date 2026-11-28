
CREATE TABLE device_event (
  id BIGINT PRIMARY KEY,
  device_id BIGINT NOT NULL,
  recorded_date TIMESTAMPTZ NOT NULL,
  start_date TIMESTAMPTZ NOT NULL,
  status_flags BIGINT NOT NULL,
  type_code VARCHAR(150) NOT NULL
);

CREATE INDEX device_event_device_start_date_idx
    ON device_event (device_id, start_date DESC, id);
