import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TranslateModule } from '@ngx-translate/core';
import { GoalService } from '../../../core/services/goal.service';
import { NotificationService } from '../../../core/services/notification.service';
import { DayEntry, HomeworkSummary } from '../../../core/models/student.model';

interface EntryState extends DayEntry {
    inputNumeric: number | null;
    inputBoolean: boolean;
}

@Component({
    selector: 'app-homework-detail',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
    templateUrl: './homework-detail.component.html',
    styleUrl: './homework-detail.component.css'
})
export class HomeworkDetailComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private goalService = inject(GoalService);
    private notifications = inject(NotificationService);
    private destroyRef = inject(DestroyRef);

    assignmentId!: number;
    homework: HomeworkSummary | null = null;
    entries: EntryState[] = [];
    isLoading = true;
    isSaving = false;

    selectedDate = '';
    minDate = '';
    maxDate = '';

    ngOnInit(): void {
        this.assignmentId = Number(this.route.snapshot.paramMap.get('assignmentId'));

        // Load homework summary to get date bounds
        this.goalService.getMyGoals()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (list) => {
                    this.homework = list.find(h => h.assignmentId === this.assignmentId) ?? null;
                    if (this.homework) {
                        this.minDate = this.homework.startDate ?? '';
                        this.maxDate = this.homework.endDate   ?? '';
                        this.selectedDate = this.clampDate(new Date().toISOString().split('T')[0]);
                    }
                    this.loadEntries();
                },
                error: () => { this.isLoading = false; }
            });
    }

    loadEntries(): void {
        this.isLoading = true;
        this.goalService.getDayEntries(this.assignmentId, this.selectedDate)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (entries) => {
                    this.entries = entries.map(e => ({
                        ...e,
                        inputNumeric: null,
                        inputBoolean: e.donePermanently || (e.booleanEntry ?? false)
                    }));
                    this.isLoading = false;
                },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to load entries'));
                    this.isLoading = false;
                }
            });
    }

    onDateChange(): void {
        this.loadEntries();
    }

    get isDayLocked(): boolean {
        return this.entries.some(e => e.dayLocked);
    }

    saveDay(): void {
        if (this.isSaving || this.isDayLocked) return;
        this.isSaving = true;

        const payload = this.entries
            .filter(e => !e.donePermanently)
            .map(e => ({
                taskId: e.taskId,
                numericValue: e.taskType === 'NUMBER' ? (e.inputNumeric ?? undefined) : undefined,
                booleanValue: e.taskType === 'CHECKBOX' ? e.inputBoolean : undefined
            }));

        this.goalService.saveDayEntries(this.assignmentId, this.selectedDate, payload)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (saved) => {
                    this.entries = saved.map(e => ({
                        ...e,
                        inputNumeric: null,
                        inputBoolean: e.donePermanently || (e.booleanEntry ?? false)
                    }));
                    this.notifications.success('Day saved successfully');
                    this.isSaving = false;
                },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to save'));
                    this.isSaving = false;
                }
            });
    }

    getProgressColor(pct: number): string {
        if (pct >= 100) return '#4ade80';
        if (pct >= 70)  return '#60a5fa';
        if (pct >= 30)  return '#facc15';
        return '#f87171';
    }

    cumulativePct(entry: EntryState): number {
        if (!entry.targetValue || entry.targetValue <= 0) return 0;
        return Math.min((entry.cumulativeValue / entry.targetValue) * 100, 100);
    }

    private clampDate(today: string): string {
        if (this.minDate && today < this.minDate) return this.minDate;
        if (this.maxDate && today > this.maxDate) return this.maxDate;
        return today;
    }
}
