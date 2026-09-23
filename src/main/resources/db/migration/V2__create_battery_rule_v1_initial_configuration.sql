INSERT INTO rule_configuration (
    id,
    rule_id,
    rule_version,
    revision,
    status,
    parameters,
    created_at,
    created_by,
    published_at,
    published_by,
    effective_from
)
VALUES (
   gen_random_uuid(),
   'BATTERY_LEVEL',
   1,
   1,
   'PUBLISHED',
   '{"threshold": 20}'::jsonb,
   CURRENT_TIMESTAMP,
   'system',
   CURRENT_TIMESTAMP,
   'system',
   CURRENT_TIMESTAMP
);