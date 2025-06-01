CREATE TABLE IF NOT EXISTS tasks
(
    task_id      UUID                   DEFAULT random_uuid(),
    name         VARCHAR(255)  NOT NULL,
    details      VARCHAR(1024) NULL,
    completed_at TIMESTAMP     NULL,
    created_at   TIMESTAMP     NOT NULL DEFAULT now()
);
