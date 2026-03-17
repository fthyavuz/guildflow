import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MeetingService } from '../../core/services/meeting.service';
import { MeetingResponse } from '../../core/models/meeting.model';
import { Observable, map } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-meeting-list',
    standalone: true,
    imports: [CommonModule, RouterModule, TranslateModule],
    templateUrl: './meeting-list.component.html',
    styleUrl: './meeting-list.component.css'
})
export class MeetingListComponent implements OnInit {
    private meetingService = inject(MeetingService);
    private authService = inject(AuthService);

    user$ = this.authService.currentUser$;
    meetings$: Observable<MeetingResponse[]> | undefined;
    upcomingMeetings$: Observable<MeetingResponse[]> | undefined;
    pastMeetings$: Observable<MeetingResponse[]> | undefined;

    ngOnInit(): void {
        const meetings$ = this.meetingService.getMyMeetings();

        this.upcomingMeetings$ = meetings$.pipe(
            map(meetings => meetings.filter(m => new Date(m.startTime) >= new Date())
                .sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime()))
        );

        this.pastMeetings$ = meetings$.pipe(
            map(meetings => meetings.filter(m => new Date(m.startTime) < new Date())
                .sort((a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime()))
        );
    }

    formatDate(dateStr: string): string {
        return new Date(dateStr).toLocaleDateString('en-US', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }

    formatTime(dateStr: string): string {
        return new Date(dateStr).toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }
}
