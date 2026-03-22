import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GoalService } from '../../core/services/goal.service';
import { NotificationService } from '../../core/services/notification.service';
import { GoalProgress, TaskProgress } from '../../core/models/student.model';
import { Observable, shareReplay } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { RouterModule } from '@angular/router';

@Component({
    selector: 'app-goal-tracking',
    standalone: true,
    imports: [CommonModule, FormsModule, TranslateModule, RouterModule],
    templateUrl: './goal-tracking.component.html',
    styleUrl: './goal-tracking.component.css'
})
export class GoalTrackingComponent implements OnInit {
    private goalService = inject(GoalService);
    private destroyRef = inject(DestroyRef);
    private notifications = inject(NotificationService);

    goals$: Observable<GoalProgress[]> | undefined;
    selectedTask: TaskProgress | null = null;
    submissionValue: number | null = null;
    isSubmitting = false;

    ngOnInit(): void {
        this.loadGoals();
    }

    loadGoals(): void {
        this.goals$ = this.goalService.getMyGoals().pipe(shareReplay(1));
    }

    openSubmitDialog(task: any): void {
        this.selectedTask = task;
        this.submissionValue = task.taskType === 'NUMBER' ? null : null;
    }

    closeDialog(): void {
        this.selectedTask = null;
        this.submissionValue = null;
    }

    quickLog(task: any): void {
        this.isSubmitting = true;
        const isNumeric = task.taskType === 'NUMBER';

        this.goalService.submitProgress(
            task.taskId,
            isNumeric ? 1 : undefined,
            !isNumeric ? true : undefined
        ).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: () => {
                this.loadGoals();
                this.isSubmitting = false;
            },
            error: (err) => {
                this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to log progress'));
                this.isSubmitting = false;
            }
        });
    }

    submitProgress(): void {
        if (!this.selectedTask) return;

        this.isSubmitting = true;
        const isNumeric = this.selectedTask.taskType === 'NUMBER';

        this.goalService.submitProgress(
            this.selectedTask.taskId,
            isNumeric ? (this.submissionValue || 0) : undefined,
            !isNumeric ? true : undefined
        ).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: () => {
                this.loadGoals();
                this.closeDialog();
                this.isSubmitting = false;
            },
            error: (err) => {
                this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to submit progress'));
                this.isSubmitting = false;
            }
        });
    }

    getProgressColor(percentage: number): string {
        if (percentage >= 100) return '#4ade80';
        if (percentage >= 70) return '#60a5fa';
        if (percentage >= 30) return '#facc15';
        return '#f87171';
    }
}
