CREATE TABLE indication_accesses
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    indicator_id    UUID NOT NULL,
    event_id        UUID NOT NULL,
    subscription_id UUID NOT NULL,

    FOREIGN KEY (indicator_id) REFERENCES users (id),
    FOREIGN KEY (event_id) REFERENCES events (id),
    FOREIGN KEY (subscription_id) REFERENCES subscriptions (id) ON DELETE CASCADE
)