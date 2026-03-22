package com.guildflow.backend.repository;

import com.guildflow.backend.model.ClassStudent;
import com.guildflow.backend.model.MentorClass;
import com.guildflow.backend.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassStudentRepository extends JpaRepository<ClassStudent, Long> {
    Optional<ClassStudent> findByStudentAndActiveTrue(User student);

    List<ClassStudent> findByMentorClassAndActiveTrue(MentorClass mentorClass);

    Optional<ClassStudent> findByMentorClassAndStudent(MentorClass mentorClass, User student);

    boolean existsByMentorClassAndStudentAndActiveTrue(MentorClass mentorClass, User student);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cs FROM ClassStudent cs WHERE cs.mentorClass = :mentorClass AND cs.student = :student")
    Optional<ClassStudent> findByMentorClassAndStudentForUpdate(@Param("mentorClass") MentorClass mentorClass, @Param("student") User student);
}
