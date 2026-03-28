package com.guildflow.backend.repository;

import com.guildflow.backend.model.ParentStudent;
import com.guildflow.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    List<ParentStudent> findByParent(User parent);
    List<ParentStudent> findByStudent(User student);
    Optional<ParentStudent> findByParentAndStudent(User parent, User student);
    boolean existsByParentAndStudent(User parent, User student);
}
