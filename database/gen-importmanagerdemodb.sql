DROP DATABASE IF EXISTS importmanagerdemodb;
CREATE DATABASE importmanagerdemodb;
USE importmanagerdemodb;

CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  uuid BINARY(16) NOT NULL UNIQUE,
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  oauth_provider VARCHAR(255),
  oauth_user_id VARCHAR(255) UNIQUE,
  email VARCHAR(255) UNIQUE,
  password VARCHAR(255),
  last_login_at DATETIME,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  modified_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      CHECK (
        (email IS NOT NULL AND password IS NOT NULL)
        OR (oauth_provider IS NOT NULL AND oauth_user_id IS NOT NULL)
    )
);

CREATE TABLE imports (
  id INT AUTO_INCREMENT PRIMARY KEY,
  uuid BINARY(16) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  status VARCHAR(255),
  records_imported int DEFAULT NULL,
  total_records int DEFAULT NULL,
  progress int,
  email VARCHAR(255),
  email_notification TINYINT,
  hubspot_list_id VARCHAR(50),
  user_uuid BINARY(16),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  modified_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_uuid) REFERENCES users(uuid)
  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE connections (
  id INT AUTO_INCREMENT PRIMARY KEY,
  uuid BINARY(16) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  type VARCHAR(50),
  five9_username VARCHAR(255),
  five9_password VARCHAR(255),
  hubspot_access_token VARCHAR(255),
  status VARCHAR(255) DEFAULT 'DISCONNECTED',
  import_id INT,
  user_uuid BINARY(16),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  modified_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (import_id) REFERENCES imports(id) ON UPDATE CASCADE ON DELETE SET NULL,
  FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE import_schedules (
  id INT AUTO_INCREMENT PRIMARY KEY,
  uuid BINARY(16) NOT NULL UNIQUE,
  start_datetime DATETIME,
  completion_datetime DATETIME,
  day INT,
  month INT,
  sunday TINYINT,
  monday TINYINT,
  tuesday TINYINT,
  wednesday TINYINT,
  thursday TINYINT,
  friday TINYINT,
  saturday TINYINT,
  recurring TINYINT,
  yearly TINYINT,
  import_id INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  modified_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (import_id) REFERENCES imports(id)
  ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE mapping_formats (
  id INT AUTO_INCREMENT PRIMARY KEY,
  uuid BINARY(16) NOT NULL UNIQUE,
  format VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  modified_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE connection_import_mappings (
  uuid BINARY(16) NOT NULL UNIQUE,
  sending_connection_id INT NOT NULL,
  receiving_connection_id INT NOT NULL,
  sending_connection_field_name VARCHAR(255) NOT NULL,
  receiving_connection_field_name VARCHAR(255) NOT NULL,
  import_id INT NOT NULL,
  mapping_format_id INT,
  five9_key TINYINT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  modified_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (sending_connection_id, receiving_connection_id, import_id, sending_connection_field_name, receiving_connection_field_name),
  FOREIGN KEY (sending_connection_id) REFERENCES connections(id) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (receiving_connection_id) REFERENCES connections(id) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (import_id) REFERENCES imports(id) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (mapping_format_id) REFERENCES mapping_formats(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE user_security_questions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  uuid BINARY(16) NOT NULL UNIQUE,
  user_id INT,
  question VARCHAR(255),
  answer VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  modified_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE
);

INSERT INTO mapping_formats (uuid, format)
VALUES (UUID_TO_BIN(UUID()), 'No Format'), (UUID_TO_BIN(UUID()), '##########');

-- Trigger for INSERT
DELIMITER //
CREATE TRIGGER set_five9_key_on_insert
BEFORE INSERT ON connection_import_mappings
FOR EACH ROW
BEGIN
    IF NEW.receiving_connection_field_name = 'number1' THEN
        SET NEW.five9_key = 1;
    END IF;
END//
DELIMITER ;



select * from imports;


