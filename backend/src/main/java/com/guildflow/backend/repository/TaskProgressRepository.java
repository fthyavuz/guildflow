package com.guildflow.backend.repository;

import com.guildflow.backend.model.GoalTask;
import com.guildflow.backend.model.TaskProgress;
import com.guildflow.backend.model.User;
import com.guildflow.backend.model.enums.ProgressEntryStatus;
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

    @Query("SELECT tp FROM TaskProgress tp JOIN FETCH tp.task t JOIN FETCH t.goal g JOIN FETCH tp.student s WHERE tp.status = :status ORDER BY tp.createdAt DESC")
    List<TaskProgress> findByStatusWithDetails(@Param("status") ProgressEntryStatus status);

    @Query("SELECT tp FROM TaskProgress tp WHERE tp.task.id IN :taskIds AND tp.student = :student AND tp.status = :status ORDER BY tp.entryDate DESC")
    List<TaskProgress> findByTaskIdsAndStudentAndStatus(@Param("taskIds") List<Long> taskIds, @Param("student") User student, @Param("status") ProgressEntryStatus status);

    @Query("SELECT tp FROM TaskProgress tp WHERE tp.task.id IN :taskIds AND tp.student = :student AND tp.entryDate = :date")
    List<TaskProgress> findByTaskIdsAndStudentAndDate(@Param("taskIds") List<Long> taskIds, @Param("student") User student, @Param("date") LocalDate date);

    /** True if this student has permanently completed this CHECKBOX task. */
    boolean existsByTaskAndStudentAndDonePermanentlyTrue(GoalTask task, User student);

    /** Find entries for a specific day to check lock status. */
    List<TaskProgress> findByStudentAndEntryDateAndLockedTrue(User student, LocalDate entryDate);

    /** Cumulative sum queries used by the report service. */
    @Query("SELECT COALESCE(SUM(tp.numericValue), 0) FROM TaskProgress tp WHERE tp.task = :task AND tp.student = :student")
    Double sumNumericValueByTaskAndStudent(@Param("task") GoalTask task, @Param("student") User student);

    /** Daily entries for a set of tasks + student within a date range, for category-level charting. */
    @Query("SELECT tp FROM TaskProgress tp WHERE tp.task IN :tasks AND tp.student = :student AND tp.entryDate BETWEEN :start AND :end ORDER BY tp.entryDate ASC")
    List<TaskProgress> findByTasksAndStudentAndDateBetween(
            @Param("tasks") List<GoalTask> tasks,
            @Param("student") User student,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
