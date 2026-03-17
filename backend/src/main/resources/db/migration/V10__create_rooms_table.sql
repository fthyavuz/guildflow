CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    capacity INT NOT NULL,
    can_stay_overnight BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE room_bookings (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    booked_by_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    FOREIGN KEY (booked_by_id) REFERENCES users(id)
);
