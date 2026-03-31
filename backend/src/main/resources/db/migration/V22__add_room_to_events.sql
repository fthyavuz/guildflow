-- Optional room booking for events
-- room_id: which room is used (informational, nullable)
-- room_booking_id: the actual booking record that was created when the event was saved
ALTER TABLE events
    ADD COLUMN room_id         BIGINT REFERENCES rooms(id)         ON DELETE SET NULL,
    ADD COLUMN room_booking_id BIGINT REFERENCES room_bookings(id) ON DELETE SET NULL;
