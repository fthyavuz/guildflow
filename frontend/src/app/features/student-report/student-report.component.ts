import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TranslateModule } from '@ngx-translate/core';
import { GoalService } from '../../core/services/goal.service';
import { NotificationService } from '../../core/services/notification.service';
import { StudentReport, StudentSummary, ReportTaskItem } from '../../core/models/student.model';

@Component({
    selector: 'app-student-report',
    standalone: true,
    imports: [CommonModule, RouterModule, TranslateModule],
    templateUrl: './student-report.component.html',
    styleUrl: './student-report.component.css'
})
export class StudentReportComponent implements OnInit {
    private goalService = inject(GoalService);
    private notifications = inject(NotificationService);
    private destroyRef = inject(DestroyRef);

    students: StudentSummary[] = [];
    selectedStudent: StudentReport | null = null;
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

    selectStudent(student: StudentSummary): void {
        if (this.selectedStudent?.studentId === student.studentId) return;
        this.selectedStudent = null;
        this.isLoadingReport = true;

        this.goalService.getStudentReport(student.studentId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (report) => {
                    this.selectedStudent = {
                        ...report,
                        inProgress: report.inProgress ?? [],
                        finished: report.finished ?? []
                    };
                    this.isLoadingReport = false;
                },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to load report'));
                    this.isLoadingReport = false;
                }
            });
    }

    approve(task: ReportTaskItem): void {
        if (!this.selectedStudent) return;
        this.goalService.approveTask(this.selectedStudent.studentId, task.assignmentId, task.taskId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    task.approved = true;
                    task.approvedAt = new Date().toISOString();
                    this.notifications.success('Task approved');
                },
                error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to approve'))
            });
    }

    revoke(task: ReportTaskItem): void {
        if (!this.selectedStudent) return;
        this.goalService.revokeApproval(this.selectedStudent.studentId, task.assignmentId, task.taskId)
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

    get hasAnyData(): boolean {
        if (!this.selectedStudent) return false;
        return (this.selectedStudent.inProgress?.length ?? 0) > 0
            || (this.selectedStudent.finished?.length ?? 0) > 0;
    }

    totalInProgress(): number {
        return this.selectedStudent?.inProgress?.reduce((sum, cat) => sum + cat.tasks.length, 0) ?? 0;
    }

    totalFinished(): number {
        return this.selectedStudent?.finished?.reduce((sum, cat) => sum + cat.tasks.length, 0) ?? 0;
    }

    getProgressColor(pct: number): string {
        if (pct >= 100) return '#4ade80';
        if (pct >= 70)  return '#60a5fa';
        if (pct >= 30)  return '#facc15';
        return '#f87171';
    }
}
