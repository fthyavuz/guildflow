import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TranslateModule } from '@ngx-translate/core';
import { GoalService } from '../../../core/services/goal.service';
import { NotificationService } from '../../../core/services/notification.service';
import { HomeworkSummary } from '../../../core/models/student.model';

@Component({
    selector: 'app-homework-list',
    standalone: true,
    imports: [CommonModule, RouterModule, TranslateModule],
    templateUrl: './homework-list.component.html',
    styleUrl: './homework-list.component.css'
})
export class HomeworkListComponent implements OnInit {
    private goalService = inject(GoalService);
    private notifications = inject(NotificationService);
    private router = inject(Router);
    private destroyRef = inject(DestroyRef);

    homeworks: HomeworkSummary[] = [];
    isLoading = true;

    ngOnInit(): void {
        this.goalService.getMyGoals()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (list) => { this.homeworks = list; this.isLoading = false; },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to load homework'));
                    this.isLoading = false;
                }
            });
    }

    openHomework(hw: HomeworkSummary): void {
        this.router.navigate(['/goals', hw.assignmentId]);
    }

    getProgressColor(pct: number): string {
        if (pct >= 100) return '#4ade80';
        if (pct >= 70)  return '#60a5fa';
        if (pct >= 30)  return '#facc15';
        return '#f87171';
    }

    isActive(hw: HomeworkSummary): boolean {
        const today = new Date().toISOString().split('T')[0];
        return (!hw.startDate || hw.startDate <= today) &&
               (!hw.endDate   || hw.endDate   >= today);
    }

    dateRange(hw: HomeworkSummary): string {
        if (!hw.startDate && !hw.endDate) return '';
        const fmt = (d: string) => d ? new Date(d).toLocaleDateString() : '—';
        return `${fmt(hw.startDate)} → ${fmt(hw.endDate)}`;
    }
}
