-- used for the runs in this report (SQLite via JDBC)
CREATE TABLE bus_pass (
  pass_id  VARCHAR(10) PRIMARY KEY,
  reg_no   VARCHAR(12) NOT NULL,
  name     VARCHAR(40) NOT NULL,
  route_no INT         NOT NULL,
  amount   DOUBLE      NOT NULL
);
