-- Campus Event Registration - MySQL 8
CREATE DATABASE IF NOT EXISTS campus_events CHARACTER SET utf8mb4;
USE campus_events;

CREATE TABLE IF NOT EXISTS events (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS registrations (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    student_id    CHAR(9)      NOT NULL,
    name          VARCHAR(60)  NOT NULL,
    email         VARCHAR(100) NOT NULL,
    event_id      INT          NOT NULL,
    registered_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reg_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT uq_student_event UNIQUE (student_id, event_id)      -- one registration per student per event
);
CREATE INDEX idx_reg_event ON registrations(event_id, id);         -- fast filter + paging

INSERT IGNORE INTO events(name) VALUES ('Cultural Night'), ('Hackathon'), ('Sports Day'), ('Tech Fest');
INSERT IGNORE INTO registrations(student_id, name, email, event_id)
    SELECT '192525001', 'Arun Kumar', 'arun.kumar@simats.edu', id FROM events WHERE name = 'Hackathon';
