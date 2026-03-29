import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MeetingService } from '../../../core/services/meeting.service';
import { ClassService } from '../../../core/services/class.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { MeetingResponse, AttendanceRecord, AttendanceStatus } from '../../../core/models/meeting.model';
import { User, UserResponse } from '../../../core/models/auth.model';

interface AttendanceRow {
    studentId: number;
    studentName: string;
    status: AttendanceStatus;
    note: string;
    saved: boolean; // had a record already
}

@Component({
    selector: 'app-meeting-detail',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule],
    templateUrl: './meeting-detail.component.html',
    styleUrl: './meeting-detail.component.css'
})
export class MeetingDetailComponent implements OnInit, OnDestroy {
    private route = inject(ActivatedRoute);
    private meetingService = inject(MeetingService);
    private classService = inject(ClassService);
    private authService = inject(AuthService);
    private notifications = inject(NotificationService);
    private destroy$ = new Subject<void>();

    meeting: MeetingResponse | null = null;
    rows: AttendanceRow[] = [];
    currentUser: User | null = null;
    isLoading = true;
    isSaving = false;

    readonly statuses: AttendanceStatus[] = ['PRESENT', 'ABSENT', 'EXCUSED', 'LATE'];

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe(u => {
            this.currentUser = u;
        });
        this.load(id);
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    private load(meetingId: number): void {
        this.isLoading = true;
        this.meetingService.getMeetingById(meetingId)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (meeting) => {
                    this.meeting = meeting;
                    forkJoin({
                        students: this.classService.getClassStudents(meeting.classId),
                        attendance: this.meetingService.getAttendance(meetingId)
                    }).pipe(takeUntil(this.destroy$)).subscribe({
                        next: ({ students, attendance }) => {
                            this.buildRows(students, attendance);
                            this.isLoading = false;
                        },
                        error: () => { this.isLoading = false; }
                    });
                },
                error: () => { this.isLoading = false; }
            });
    }

    private buildRows(students: UserResponse[], attendance: AttendanceRecord[]): void {
        const recordMap = new Map(attendance.map(a => [a.studentId, a]));
        this.rows = students.map(s => {
            const existing = recordMap.get(s.id);
            return {
                studentId: s.id,
                studentName: `${s.firstName} ${s.lastName}`,
                status: existing?.status ?? 'PRESENT',
                note: existing?.note ?? '',
                saved: !!existing
            };
        });
    }

    /** Whether the meeting is in the past (start time already passed). */
    get isPast(): boolean {
        if (!this.meeting) return false;
        return new Date(this.meeting.startTime) < new Date();
    }

    /**
     * Whether the current user can edit attendance:
     * - ADMIN: any past meeting
     * - MENTOR: past meeting within the last 30 days
     */
    get canEdit(): boolean {
        if (!this.isPast || !this.currentUser) return false;
        if (this.currentUser.role === 'ADMIN') return true;
        if (this.currentUser.role === 'MENTOR') {
            const oneMonthAgo = new Date();
            oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1);
            return new Date(this.meeting!.startTime) >= oneMonthAgo;
        }
        return false;
    }

    get editLockReason(): string {
        if (!this.isPast) return 'Attendance can only be recorded after the meeting has started.';
        if (this.currentUser?.role === 'MENTOR') return 'Mentors can only edit attendance for meetings within the last month.';
        return '';
    }

    markAll(status: AttendanceStatus): void {
        this.rows.forEach(r => r.status = status);
    }

    save(): void {
        if (!this.meeting || !this.canEdit) return;
        this.isSaving = true;
        const payload = this.rows.map(r => ({
            studentId: r.studentId,
            status: r.status,
            note: r.note
        }));
        this.meetingService.markAttendance(this.meeting.id, payload)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: () => {
                    this.notifications.success('Attendance saved');
                    this.rows.forEach(r => r.saved = true);
                    this.isSaving = false;
                },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to save attendance'));
                    this.isSaving = false;
                }
            });
    }

    statusLabel(s: AttendanceStatus): string {
        const map: Record<AttendanceStatus, string> = {
            PRESENT: 'Present',
            ABSENT: 'Absent',
            EXCUSED: 'Excused',
            LATE: 'Late'
        };
        return map[s];
    }

    statusClass(s: AttendanceStatus): string {
        const map: Record<AttendanceStatus, string> = {
            PRESENT: 'status-present',
            ABSENT: 'status-absent',
            EXCUSED: 'status-excused',
            LATE: 'status-late'
        };
        return map[s];
    }

    get presentCount(): number { return this.rows.filter(r => r.status === 'PRESENT').length; }
    get absentCount(): number { return this.rows.filter(r => r.status === 'ABSENT').length; }
    get lateCount(): number { return this.rows.filter(r => r.status === 'LATE').length; }
    get excusedCount(): number { return this.rows.filter(r => r.status === 'EXCUSED').length; }
}
