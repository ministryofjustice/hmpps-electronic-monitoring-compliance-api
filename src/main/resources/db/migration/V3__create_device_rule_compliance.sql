CREATE TABLE device_compliance
(
    id UUID PRIMARY KEY,
    device_id INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    state VARCHAR(50),

    CONSTRAINT device_compliance_device_id_unique
        UNIQUE (device_id)
);

CREATE TABLE device_rule_compliance
(
    id UUID PRIMARY KEY,
    device_compliance_id UUID NOT NULL,
    device_id INTEGER NOT NULL,
    rule_id VARCHAR(100) NOT NULL,
    rule_version INTEGER NOT NULL,
    state VARCHAR(50) NOT NULL,
    state_changed_at TIMESTAMPTZ,

    CONSTRAINT device_rule_compliance_device_compliance_fk
        FOREIGN KEY (device_compliance_id)
            REFERENCES device_compliance(id),

    CONSTRAINT device_rule_compliance_rule_unique
        UNIQUE (
                device_compliance_id,
                rule_id,
                rule_version
            )
);

CREATE INDEX device_rule_compliance_device_id_idx
    ON device_rule_compliance(device_id);

CREATE INDEX device_rule_compliance_rule_idx
    ON device_rule_compliance(rule_id, rule_version);