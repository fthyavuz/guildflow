import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TranslateModule } from '@ngx-translate/core';
import { GoalService } from '../../core/services/goal.service';
import { NotificationService } from '../../core/services/notification.service';
import { StudentReport, AssignmentReport, TaskReport } from '../../core/models/student.model';

@Component({
    selector: 'app-student-report',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
    templateUrl: './student-report.component.html',
    styleUrl: './student-report.component.css'
})
export class StudentReportComponent implements OnInit {
    private goalService = inject(GoalService);
    private notifications = inject(NotificationService);
    private destroyRef = inject(DestroyRef);

    students: StudentReport[] = [];
    selectedStudent: StudentReport | null = null;
    expandedAssignments = new Set<number>();
    isLoadingList = true;
    isLoadingReport = false;

    ngOnInit(): void {
        this.goalService.getStudentList()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (list) => { this.students = list; this.isLoadingList = false; },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to load students'));
                    this.isLoadingList = false;
                }
            });
    }

    selectStudent(student: StudentReport): void {
        if (this.selectedStudent?.studentId === student.studentId) return;
        this.selectedStudent = null;
        this.expandedAssignments.clear();
        this.isLoadingReport = true;

        this.goalService.getStudentReport(student.studentId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (report) => { this.selectedStudent = report; this.isLoadingReport = false; },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to load report'));
                    this.isLoadingReport = false;
                }
            });
    }

    toggleAssignment(id: number): void {
        this.expandedAssignments.has(id)
            ? this.expandedAssignments.delete(id)
            : this.expandedAssignments.add(id);
    }

    isExpanded(id: number): boolean {
        return this.expandedAssignments.has(id);
    }

    approve(assignment: AssignmentReport, task: TaskReport): void {
        if (!this.selectedStudent) return;
        this.goalService.approveTask(this.selectedStudent.studentId, assignment.assignmentId, task.taskId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    task.approved = true;
                    task.approvedAt = new Date().toISOString();
                    this.notifications.success('Task marked as completed');
                },
                error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to approve'))
            });
    }

    revoke(assignment: AssignmentReport, task: TaskReport): void {
        if (!this.selectedStudent) return;
        this.goalService.revokeApproval(this.selectedStudent.studentId, assignment.assignmentId, task.taskId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    task.approved = false;
                    task.approvedAt = undefined;
                    task.approvedByName = undefined;
                    this.notifications.success('Approval revoked');
                },
                error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to revoke'))
            });
    }

    getProgressColor(pct: number): string {
        if (pct >= 100) return '#4ade80';
        if (pct >= 70)  return '#60a5fa';
        if (pct >= 30)  return '#facc15';
        return '#f87171';
    }

    overallAssignmentProgress(a: AssignmentReport): number {
        if (!a.tasks.length) return 0;
        return a.tasks.reduce((s, t) => s + t.progressPercentage, 0) / a.tasks.length;
    }
}
