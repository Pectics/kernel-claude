-- PostgreSQL Schema for Perms Module
-- Licensed under MIT License

-- Users table: stores user metadata
CREATE TABLE IF NOT EXISTS "{prefix}users" (
    "user_id"       VARCHAR(32)  NOT NULL,
    "platform"      VARCHAR(64)  NOT NULL,
    "native_id"     VARCHAR(255) NOT NULL,
    "primary_group" VARCHAR(64)  NOT NULL DEFAULT 'default',
    PRIMARY KEY ("user_id")
);

CREATE INDEX IF NOT EXISTS "{prefix}users_platform_native" ON "{prefix}users" ("platform", "native_id");

-- User nodes table: stores permission, inheritance, meta nodes
CREATE TABLE IF NOT EXISTS "{prefix}user_nodes" (
    "id"         SERIAL NOT NULL,
    "user_id"    VARCHAR(32)  NOT NULL,
    "node_key"   VARCHAR(200) NOT NULL,
    "node_value" BOOLEAN      NOT NULL DEFAULT TRUE,
    "expire_at"  BIGINT       NOT NULL DEFAULT 0,
    "contexts"   VARCHAR(512) NOT NULL,
    PRIMARY KEY ("id")
);

CREATE INDEX IF NOT EXISTS "{prefix}user_nodes_user" ON "{prefix}user_nodes" ("user_id");

-- Groups table
CREATE TABLE IF NOT EXISTS "{prefix}groups" (
    "group_id" VARCHAR(64) NOT NULL,
    PRIMARY KEY ("group_id")
);

-- Group nodes table
CREATE TABLE IF NOT EXISTS "{prefix}group_nodes" (
    "id"         SERIAL NOT NULL,
    "group_id"   VARCHAR(64)  NOT NULL,
    "node_key"   VARCHAR(200) NOT NULL,
    "node_value" BOOLEAN      NOT NULL DEFAULT TRUE,
    "expire_at"  BIGINT       NOT NULL DEFAULT 0,
    "contexts"   VARCHAR(512) NOT NULL,
    PRIMARY KEY ("id")
);

CREATE INDEX IF NOT EXISTS "{prefix}group_nodes_group" ON "{prefix}group_nodes" ("group_id");
