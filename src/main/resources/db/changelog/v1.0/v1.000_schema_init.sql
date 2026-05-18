CREATE TABLE request (
     id UUID PRIMARY KEY,
     user_id UUID NOT NULL,
     query_text VARCHAR(400) NOT NULL,
     family_mode VARCHAR(50),
     groups_on_page VARCHAR(20),
     updated TIMESTAMP WITH TIME ZONE,
     created TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE request_result (
    id UUID PRIMARY KEY
    status VARCHAR(50) NOT NULL,
    saved_copy_url TEXT,
    url TEXT NOT NULL,
    domain VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    mod_time TIMESTAMP WITH TIME ZONE,
    size BIGINT,
    charset VARCHAR(50),
    mime_type VARCHAR(100),
    passages TEXT NOT NULL,
    request_id UUID,
    created TIMESTAMP WITH TIME ZONE,
    updated TIMESTAMP WITH TIME ZONE NOT NULL
);