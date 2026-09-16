CREATE TABLE device_rule_compliance
(
    id UUID PRIMARY KEY,
    device_id INTEGER NOT NULL,
    rule_id VARCHAR(100) NOT NULL,
    rule_version INTEGER NOT NULL,
    state VARCHAR(50) NOT NULL,
    state_changed_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT device_rule_compliance_device_rule_version_unique
        UNIQUE (device_id, rule_id, rule_version)
);