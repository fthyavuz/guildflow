import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GoalService } from '../../core/services/goal.service';
import { NotificationService } from '../../core/services/notification.service';
import { GoalProgress, TaskProgress } from '../../core/models/student.model';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { RouterModule } from '@angular/router';

interface TaskEntry {
    task: TaskProgress;
    inputValue: number | null;
    checked: boolean;
    submitting: boolean;
    submitted: boolean;
}

interface GoalView {
    goal: GoalProgress;
    entries: TaskEntry[];
}

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

    selectedDate: string = new Date().toISOString().split('T')[0];
    goalViews: GoalView[] = [];
    isLoading = true;

    ngOnInit(): void {
        this.loadGoals();
    }

    loadGoals(): void {
        this.isLoading = true;
        this.goalService.getMyGoals()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (goals) => {
                    this.goalViews = goals.map(goal => ({
                        goal,
                        entries: goal.tasks.map(task => ({
                            task,
                            inputValue: null,
                            checked: false,
                            submitting: false,
                            submitted: false
                        }))
                    }));
                    this.isLoading = false;
                },
                error: () => { this.isLoading = false; }
            });
    }

    onDateChange(): void {
        // Reset submitted flags when date changes so user can re-enter
        this.goalViews.forEach(gv => {
            gv.entries.forEach(e => {
                e.submitted = false;
                e.inputValue = null;
                e.checked = false;
            });
        });
    }

    submitEntry(entry: TaskEntry): void {
        if (entry.submitting) return;
        entry.submitting = true;

        const isNumeric = entry.task.taskType === 'NUMBER';
        this.goalService.submitProgress(
            entry.task.taskId,
            this.selectedDate,
            isNumeric ? (entry.inputValue ?? 0) : undefined,
            !isNumeric ? entry.checked : undefined
        ).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: () => {
                entry.submitted = true;
                entry.submitting = false;
                this.notifications.success('Progress submitted — awaiting mentor approval');
            },
            error: (err) => {
                entry.submitting = false;
                this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to submit'));
            }
        });
    }

    totalEntriesForDate(): number {
        return this.goalViews.reduce((sum, gv) =>
            sum + gv.entries.filter(e => e.submitted).length, 0);
    }

    getProgressColor(percentage: number): string {
        if (percentage >= 100) return '#4ade80';
        if (percentage >= 70) return '#60a5fa';
        if (percentage >= 30) return '#facc15';
        return '#f87171';
    }

    getStatusClass(status?: string): string {
        if (status === 'APPROVED') return 'status-approved';
        if (status === 'REJECTED') return 'status-rejected';
        return 'status-pending';
    }
}
