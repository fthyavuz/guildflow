package com.guildflow.backend.repository;

import com.guildflow.backend.model.GoalTask;
import com.guildflow.backend.model.TaskProgress;
import com.guildflow.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskProgressRepository extends JpaRepository<TaskProgress, Long> {
    List<TaskProgress> findByTaskAndStudentOrderByEntryDateDesc(GoalTask task, User student);

    @Query("SELECT tp FROM TaskProgress tp WHERE tp.task.id IN :taskIds AND tp.student = :student ORDER BY tp.entryDate DESC")
    List<TaskProgress> findByTaskIdsAndStudent(@Param("taskIds") List<Long> taskIds, @Param("student") User student);

    @Query("SELECT tp FROM TaskProgress tp WHERE tp.task.id IN :taskIds AND tp.student IN :students")
    List<TaskProgress> findByTaskIdsAndStudents(@Param("taskIds") List<Long> taskIds, @Param("students") List<User> students);

    Optional<TaskProgress> findByTaskAndStudentAndEntryDate(GoalTask task, User student, LocalDate entryDate);

    List<TaskProgress> findByStudentAndEntryDateBetween(User student, LocalDate start, LocalDate end);
}
