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
                        const today = this.localDateStr(new Date());
                        const sevenDaysAgo = this.shiftDate(today, -6);
                        const startDate = this.homework.startDate ?? '';
                        this.minDate = startDate > sevenDaysAgo ? startDate : sevenDaysAgo;
                        const endDate = this.homework.endDate ?? '';
                        this.maxDate = endDate && endDate < today ? endDate : today;
                        this.selectedDate = this.clampDate(today);
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

    get canGoPrev(): boolean {
        return !!this.minDate && this.selectedDate > this.minDate;
    }

    get canGoNext(): boolean {
        return !!this.maxDate && this.selectedDate < this.maxDate;
    }

    prevDay(): void {
        if (!this.canGoPrev) return;
        this.selectedDate = this.shiftDate(this.selectedDate, -1);
        this.loadEntries();
    }

    nextDay(): void {
        if (!this.canGoNext) return;
        this.selectedDate = this.shiftDate(this.selectedDate, 1);
        this.loadEntries();
    }

    private shiftDate(dateStr: string, days: number): string {
        const d = new Date(dateStr + 'T00:00:00');
        d.setDate(d.getDate() + days);
        return this.localDateStr(d);
    }

    private localDateStr(d: Date): string {
        const yyyy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}`;
    }

    get isDayLocked(): boolean {
        return this.entries.some(e => e.dayLocked);
    }

    get hasLimitViolation(): boolean {
        return this.entries.some(e =>
            e.taskType === 'NUMBER' && e.dailyLimit != null && (e.inputNumeric ?? 0) > e.dailyLimit
        );
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
                next: () => {
                    this.notifications.success('Day saved successfully');
                    this.isSaving = false;
                    this.loadEntries();
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
