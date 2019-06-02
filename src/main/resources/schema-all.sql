DROP TABLE bit_coin_rates IF EXISTS;

CREATE TABLE bit_coin_rates (
  id            BIGINT IDENTITY NOT NULL PRIMARY KEY,
  rate          DOUBLE PRECISION NOT NULL,
  date          DATE NOT NULL,
  last_modified TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX date_idx ON bit_coin_rates(date);
