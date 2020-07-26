CREATE TABLE employee (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(250) NOT NULL,
  username VARCHAR(250) NOT NULL,
  email VARCHAR(250) NOT NULL,
  phone_number VARCHAR(10) NOT NULL,
  age INT NOT NULL
);
ALTER TABLE employee ADD CONSTRAINT employee_unique_username UNIQUE(username);
ALTER TABLE employee ADD CONSTRAINT employee_unique_email UNIQUE(email);
ALTER TABLE employee ADD CONSTRAINT employee_unique_phone_number UNIQUE(phone_number);

CREATE TABLE rate_alert (
  id INT AUTO_INCREMENT PRIMARY KEY,
  base CHAR(3) NOT NULL,
  email VARCHAR(250) NOT NULL
);
ALTER TABLE rate_alert ADD CONSTRAINT rate_alert_unique_email UNIQUE(email);