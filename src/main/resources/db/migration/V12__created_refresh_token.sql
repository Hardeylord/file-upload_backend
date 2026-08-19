CREATE TABLE RefreshToken (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token varchar(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    revoked boolean NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_RefreshToken_user
    FOREIGN KEY (user_id)
    REFERENCES Users(id)
    ON DELETE CASCADE
)