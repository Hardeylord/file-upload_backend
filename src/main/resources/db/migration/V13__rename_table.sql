ALTER TABLE refreshtoken
RENAME TO refresh_token;

ALTER TABLE refresh_token
RENAME CONSTRAINT fk_refreshtoken_user
TO fk_refresh_token_user;