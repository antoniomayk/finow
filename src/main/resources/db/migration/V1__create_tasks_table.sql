CREATE TABLE IF NOT EXISTS tasks
(
  task_id UUID NOT NULL,
  task_name VARCHAR(255) NOT NULL,
  task_details VARCHAR(1024) NULL,
  completed_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now()
);
