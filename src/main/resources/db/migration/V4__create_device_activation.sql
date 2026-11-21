CREATE TABLE device_activation (
   id BIGINT PRIMARY KEY,
   device_id BIGINT NOT NULL,
   activation_date TIMESTAMPTZ NOT NULL,
   deactivation_date TIMESTAMPTZ,
   discovered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
   updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

   CONSTRAINT device_activation_identity_unique
       UNIQUE (device_id, activation_date),

   CONSTRAINT device_activation_dates_valid
       CHECK (
           deactivation_date IS NULL
               OR deactivation_date >= activation_date
           )
);

CREATE INDEX device_activation_active_idx
    ON device_activation (device_id)
    WHERE deactivation_date IS NULL;