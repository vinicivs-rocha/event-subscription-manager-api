CREATE TABLE users (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   name VARCHAR(50) NOT NULL UNIQUE,
   email VARCHAR(100) NOT NULL UNIQUE,
   password VARCHAR(255)
);

CREATE INDEX idx_users_email ON users (email);

CREATE TABLE events(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL UNIQUE,
    slug VARCHAR(50) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_events_slug ON events (slug);
CREATE INDEX idx_events_title ON events (title);

CREATE TABLE subscriptions (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   subscriber_id UUID NOT NULL,
   indicator_id UUID,
   event_id UUID NOT NULL,

   FOREIGN KEY (subscriber_id) REFERENCES users(id),
   FOREIGN KEY (indicator_id) REFERENCES users(id),
   FOREIGN KEY (event_id) REFERENCES events(id)
);