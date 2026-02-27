package com.guildflow.backend.repository;

import com.guildflow.backend.model.GoalTask;
import com.guildflow.backend.model.TaskProgress;
import com.guildflow.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskProgressRepository extends JpaRepository<TaskProgress, Long> {
    List<TaskProgress> findByTaskAndStudentOrderByEntryDateDesc(GoalTask task, User student);

    Optional<TaskProgress> findByTaskAndStudentAndEntryDate(GoalTask task, User student, LocalDate entryDate);

    List<TaskProgress> findByStudentAndEntryDateBetween(User student, LocalDate start, LocalDate end);
}
