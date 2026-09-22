CREATE TABLE videoTranscript (
    id UUID PRIMARY KEY NOT NULL ,
    video_id TEXT NOT NULL,
    start_offset double precision NOT NULL,
    end_offset double precision NOT NULL,
    text TEXT NOT NULL,

    FOREIGN KEY (video_id)
    REFERENCES video(id)
    ON DELETE CASCADE
)

