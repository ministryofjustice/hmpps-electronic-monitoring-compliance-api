CREATE TABLE rule_configuration
(
    id UUID PRIMARY KEY,
    rule_id VARCHAR(100) NOT NULL,
    rule_version INTEGER NOT NULL,
    revision INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    parameters JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    published_at TIMESTAMPTZ,
    published_by VARCHAR(255),
    effective_from TIMESTAMPTZ,

    CONSTRAINT rule_configuration_rule_version_revision_unique
        UNIQUE (rule_id, rule_version, revision),

    CONSTRAINT rule_configuration_publication_valid
        CHECK (
            (
                status = 'PUBLISHED'
                    AND published_at IS NOT NULL
                    AND published_by IS NOT NULL
                    AND effective_from IS NOT NULL
                )
                OR (
                status = 'DRAFT'
                    AND published_at IS NULL
                    AND published_by IS NULL
                    AND effective_from IS NULL
                )
            )
);

CREATE UNIQUE INDEX rule_configuration_one_draft_per_rule_version
    ON rule_configuration (rule_id, rule_version)
    WHERE status = 'DRAFT';
