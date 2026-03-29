import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MeetingService } from '../../core/services/meeting.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { MeetingResponse } from '../../core/models/meeting.model';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-meeting-list',
    standalone: true,
    imports: [CommonModule, RouterModule, TranslateModule],
    templateUrl: './meeting-list.component.html',
    styleUrl: './meeting-list.component.css'
})
export class MeetingListComponent implements OnInit, OnDestroy {
    private meetingService = inject(MeetingService);
    private authService = inject(AuthService);
    private notifications = inject(NotificationService);
    private destroy$ = new Subject<void>();

    user$ = this.authService.currentUser$;

    isLoading = false;
    upcomingMeetings: MeetingResponse[] = [];
    pastMeetings: MeetingResponse[] = [];
    deletingId: number | null = null;

    ngOnInit(): void {
        this.loadMeetings();
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    loadMeetings(): void {
        this.isLoading = true;
        this.meetingService.getMyMeetings()
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (meetings) => {
                    const now = new Date();
                    this.upcomingMeetings = meetings
                        .filter(m => new Date(m.startTime) >= now)
                        .sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());
                    this.pastMeetings = meetings
                        .filter(m => new Date(m.startTime) < now)
                        .sort((a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime());
                    this.isLoading = false;
                },
                error: () => { this.isLoading = false; }
            });
    }

    deleteMeeting(meeting: MeetingResponse): void {
        if (!confirm(`Delete "${meeting.title}"? This cannot be undone.`)) return;
        this.deletingId = meeting.id;
        this.meetingService.deleteMeeting(meeting.id)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: () => {
                    this.notifications.success('Meeting deleted');
                    this.loadMeetings();
                    this.deletingId = null;
                },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to delete meeting'));
                    this.deletingId = null;
                }
            });
    }
}
