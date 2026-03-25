package com.guildflow.backend.repository;

import com.guildflow.backend.model.Source;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {

    @Query("SELECT s FROM Source s LEFT JOIN FETCH s.category")
    Page<Source> findAllWithCategory(Pageable pageable);
}
