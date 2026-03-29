-- Link meetings to a room and their booking
ALTER TABLE meetings
    ADD COLUMN room_id        BIGINT REFERENCES rooms(id)         ON DELETE SET NULL,
    ADD COLUMN room_booking_id BIGINT REFERENCES room_bookings(id) ON DELETE SET NULL;

CREATE INDEX idx_meetings_room_id ON meetings(room_id);
