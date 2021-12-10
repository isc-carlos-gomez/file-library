-- CREATE SCHEMA IF NOT EXISTS library;

-- CREATE TABLE library.files (
--   id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
--   path TEXT NOT NULL
--   -- hash TEXT NOT NULL
-- );

CREATE TABLE library.file_paths (
  id UUID PRIMARY KEY,
  path TEXT NOT NULL
);

CREATE TABLE library.file_attributes (
  path_id INTEGER NOT NULL,
  name TEXT NOT NULL,
  size BIGINT NOT NULL,
  last_modified_time TIMESTAMP NOT NULL
);

-- CREATE TABLE library.file_hashes (
--   path_id INTEGER NOT NULL,
--   hash TEXT NOT NULL
-- )

-- CREATE TABLE library.picture_attributes (
--   path_id UUID NOT NULL,

-- )
