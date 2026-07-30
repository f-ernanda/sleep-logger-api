CREATE TABLE sleep_log (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT      NOT NULL,
    log_date          DATE        NOT NULL,
    time_in_bed_start TIMESTAMPTZ NOT NULL,
    time_in_bed_end   TIMESTAMPTZ NOT NULL,
    feeling           VARCHAR(4)  NOT NULL CHECK (feeling IN ('BAD', 'OK', 'GOOD')),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_sleep_log_user_date ON sleep_log (user_id, log_date DESC);
