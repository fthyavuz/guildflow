import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { GoalService } from '../../../core/services/goal.service';
import { NotificationService } from '../../../core/services/notification.service';
import { PendingProgressEntry } from '../../../core/models/student.model';

interface EntryState {
    entry: PendingProgressEntry;
    mentorNotes: string;
    acting: boolean;
    done: boolean;
    action?: 'approved' | 'rejected';
}

@Component({
    selector: 'app-homework-approval',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule, TranslateModule],
    templateUrl: './homework-approval.component.html',
    styleUrl: './homework-approval.component.css'
})
export class HomeworkApprovalComponent implements OnInit {
    private goalService = inject(GoalService);
    private destroyRef = inject(DestroyRef);
    private notifications = inject(NotificationService);

    states: EntryState[] = [];
    isLoading = true;

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.isLoading = true;
        this.goalService.getPendingApprovals()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (entries) => {
                    this.states = entries.map(e => ({
                        entry: e,
                        mentorNotes: '',
                        acting: false,
                        done: false
                    }));
                    this.isLoading = false;
                },
                error: () => { this.isLoading = false; }
            });
    }

    approve(state: EntryState): void {
        state.acting = true;
        this.goalService.approveEntry(state.entry.entryId, state.mentorNotes || undefined)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    state.done = true;
                    state.action = 'approved';
                    state.acting = false;
                },
                error: (err) => {
                    state.acting = false;
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to approve'));
                }
            });
    }

    reject(state: EntryState): void {
        state.acting = true;
        this.goalService.rejectEntry(state.entry.entryId, state.mentorNotes || undefined)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    state.done = true;
                    state.action = 'rejected';
                    state.acting = false;
                },
                error: (err) => {
                    state.acting = false;
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to reject'));
                }
            });
    }

    get pending(): EntryState[] {
        return this.states.filter(s => !s.done);
    }

    get reviewed(): EntryState[] {
        return this.states.filter(s => s.done);
    }

    formatValue(state: EntryState): string {
        const e = state.entry;
        if (e.taskType === 'NUMBER') return `+${e.numericValue ?? 0}`;
        return e.booleanValue ? '✓ Done' : '✗ Not done';
    }
}
