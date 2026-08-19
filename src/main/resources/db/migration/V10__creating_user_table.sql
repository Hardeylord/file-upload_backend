CREATE TABLE Users (
    id UUID primary key,
    username varchar(50) NOT NULL UNIQUE,
    password varchar(255) NOT NULL UNIQUE,
    email varchar(255) NOT NULL UNIQUE,
    roles varchar(20) NOT NULL,
    verified boolean NOT NULL default false,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
)