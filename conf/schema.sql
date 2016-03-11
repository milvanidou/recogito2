CREATE TABLE user (
  username VARCHAR NOT NULL PRIMARY KEY,
  email VARCHAR NOT NULL,
  password_hash VARCHAR,
  salt VARCHAR,
  member_since TIMESTAMP WITH TIME ZONE NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE user_activity_log (
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
  username VARCHAR NOT NULL REFERENCES user(username),
  -- TODO do some sort of structured recording of activity types in the future
  activity VARCHAR NOT NULL
);

CREATE TABLE user_activity_per_day (
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
  username VARCHAR NOT NULL REFERENCES user(username),
  -- TODO split up totals per activity type, once we have them defined
  total_activities INTEGER NOT NULL
);

-- staging area for documents during upload workflow
CREATE TABLE upload (
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  owner VARCHAR NOT NULL UNIQUE REFERENCES user(username),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  title VARCHAR NOT NULL,
  author VARCHAR,
  date_freeform VARCHAR,
  description VARCHAR,
  source VARCHAR,
  language VARCHAR
);

CREATE TABLE upload_filepart (
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  upload_id INTEGER NOT NULL REFERENCES upload(id) ON DELETE CASCADE,
  owner VARCHAR NOT NULL REFERENCES user(username),
  title VARCHAR NOT NULL,
  content_type VARCHAR NOT NULL,
  filename VARCHAR NOT NULL,
  filesize_kb DOUBLE NOT NULL,
  -- TODO filepart metadata (source, identifier,... ?)
  UNIQUE (owner, title)
);

-- users own (and can share) documents
CREATE TABLE document (
  id VARCHAR NOT NULL PRIMARY KEY,
  owner VARCHAR NOT NULL REFERENCES user(username),
  uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL,
  title VARCHAR NOT NULL,
  author VARCHAR,
  date_numeric TIMESTAMP,
  date_freeform VARCHAR,
  description VARCHAR,
  source VARCHAR,
  language VARCHAR
);

CREATE TABLE document_filepart (
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  document_id VARCHAR NOT NULL REFERENCES document(id) ON DELETE CASCADE,
  title VARCHAR NOT NULL,
  content_type VARCHAR NOT NULL,
  filename VARCHAR NOT NULL,
  sequence_no INTEGER NOT NULL,
  -- TODO filepart metadata (source, identifier,... ?)
  UNIQUE (document_id, sequence_no)
);

-- users can organize documents into folders
CREATE TABLE folder (
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  owner VARCHAR NOT NULL REFERENCES user(username),
  title VARCHAR NOT NULL,
  -- if parent is empty then it's a root folder
  parent INTEGER REFERENCES folder(id)
);

CREATE TABLE folder_association (
  folder_id INTEGER NOT NULL REFERENCES folder(id),
  document_id INTEGER NOT NULL REFERENCES document(id)
);

-- teams are a first level entities similar to user
CREATE TABLE team (
  title VARCHAR NOT NULL PRIMARY KEY,
  created_by VARCHAR NOT NULL REFERENCES user(username),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE team_membership (
  username VARCHAR NOT NULL REFERENCES user(username),
  team VARCHAR NOT NULL REFERENCES team(title),
  member_since TIMESTAMP WITH TIME ZONE NOT NULL
);

-- ledger of shared documents and folders
CREATE TABLE sharing_policy (
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  -- one of the following two needs to be defined
  folder_id INTEGER REFERENCES folder(id),
  document_id INTEGER REFERENCES document(id),
  shared_by VARCHAR NOT NULL REFERENCES user(username),
  shared_with VARCHAR NOT NULL REFERENCES user(username),
  shared_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- keep a log of what happened for shared elements
-- e.g. to inform users about what happened
CREATE TABLE sharing_event_log (
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  type_of_action VARCHAR,
  action_by VARCHAR NOT NULL REFERENCES user(username),
  action_at TIMESTAMP WITH TIME ZONE NO NULL,
  policy_id INTEGER NOT NULL REFERENCES sharing_policy(id)
);

-- tags are user specific and allow him/her to group documents
-- CREATE TABLE hashtags (
--   id integer NOT NULL PRIMARY KEY AUTOINCREMENT,
--   userid varchar NOT NULL REFERENCES users(username),
--   name varchar NOT NULL
-- );

-- CREATE TABLE hashtags_documents(
--    tagid integer NOT NULL REFERENCES tags(id),
--    docid integer NOT NULL REFERENCES documents(id)
-- );
