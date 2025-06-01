CREATE TABLE IF NOT EXISTS tasks
(
    task_id      UUID          NOT NULL,
    name         VARCHAR(255)  NOT NULL,
    details      VARCHAR(1024) NULL,
    completed_at TIMESTAMP     NULL,
    created_at   TIMESTAMP     NOT NULL DEFAULT now()
);
