package com.guildflow.backend.repository;

import com.guildflow.backend.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByTitle(String title);
    boolean existsByTitle(String title);
}
