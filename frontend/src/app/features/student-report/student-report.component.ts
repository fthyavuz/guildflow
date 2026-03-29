import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TranslateModule } from '@ngx-translate/core';
import { GoalService } from '../../core/services/goal.service';
import { MeetingService } from '../../core/services/meeting.service';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';
import { StudentReport, StudentSummary, ReportTaskItem, DailyProgressEntry, AttendanceSummary } from '../../core/models/student.model';
import { User } from '../../core/models/auth.model';

@Component({
    selector: 'app-student-report',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
    templateUrl: './student-report.component.html',
    styleUrl: './student-report.component.css'
})
export class StudentReportComponent implements OnInit {
    private goalService = inject(GoalService);
    private meetingService = inject(MeetingService);
    private notifications = inject(NotificationService);
    private authService = inject(AuthService);
    private destroyRef = inject(DestroyRef);

    currentUser: User | null = null;

    students: StudentSummary[] = [];
    filteredStudents: StudentSummary[] = [];
    selectedStudent: StudentReport | null = null;
    isLoadingList = true;
    isLoadingReport = false;

    nameFilter = '';
    levelFilter = '';
    educationLevels: string[] = [];

    // Attendance summary
    attendanceSummary: AttendanceSummary | null = null;

    // Chart state
    chartCategory: string | null = null;
    chartStartDate = '2000-01-01';
    chartEndDate = new Date().toISOString().split('T')[0];
    chartData: DailyProgressEntry[] = [];
    isLoadingChart = false;
    chartTotal = 0;

    readonly CHART_BAR_PX = 160;
    readonly DONUT_R = 45;
    readonly DONUT_CIRCUMFERENCE = 2 * Math.PI * 45; // ≈ 282.74

    get isMentorOrAdmin(): boolean {
        return this.currentUser?.role === 'ADMIN' || this.currentUser?.role === 'MENTOR';
    }

    get isStudentOrParent(): boolean {
        return this.currentUser?.role === 'STUDENT' || this.currentUser?.role === 'PARENT';
    }

    get showSidebar(): boolean {
        return this.currentUser?.role !== 'STUDENT';
    }

    get donutSegments(): { arc: number; offset: number; color: string; label: string }[] {
        const s = this.attendanceSummary;
        if (!s || s.total === 0) return [];
        const c = this.DONUT_CIRCUMFERENCE;
        const items = [
            { count: s.present, color: '#4ade80', label: 'Present' },
            { count: s.absent,  color: '#f87171', label: 'Absent' },
            { count: s.late,    color: '#facc15', label: 'Late' },
            { count: s.excused, color: '#60a5fa', label: 'Excused' },
        ];
        const segments: { arc: number; offset: number; color: string; label: string }[] = [];
        let offset = 0;
        for (const item of items) {
            if (item.count > 0) {
                const arc = (item.count / s.total) * c;
                segments.push({ arc, offset, color: item.color, label: item.label });
                offset += arc;
            }
        }
        return segments;
    }

    get chartableCategories(): string[] {
        if (!this.selectedStudent) return [];
        const allSections = [
            ...(this.selectedStudent.inProgress ?? []),
            ...(this.selectedStudent.finished ?? [])
        ];
        const cats = new Set<string>();
        for (const sec of allSections) {
            if (sec.tasks.some(t => t.taskType === 'NUMBER')) {
                cats.add(sec.categoryName);
            }
        }
        return [...cats].sort();
    }

    get chartMaxValue(): number {
        if (!this.chartData.length) return 1;
        return Math.max(...this.chartData.map(d => d.value), 1);
    }

    ngOnInit(): void {
        this.currentUser = this.authService.getCurrentUser();

        // STUDENT auto-loads their own report — no sidebar needed
        if (this.currentUser?.role === 'STUDENT') {
            this.isLoadingList = false;
            this.loadOwnReport(this.currentUser.id);
            return;
        }

        this.goalService.getStudentList()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (list) => {
                    this.students = list;
                    this.filteredStudents = list;
                    this.educationLevels = [...new Set(list.map(s => s.educationLevel).filter((l): l is string => !!l))].sort();
                    this.isLoadingList = false;
                },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to load students'));
                    this.isLoadingList = false;
                }
            });
    }

    private loadOwnReport(studentId: number): void {
        this.isLoadingReport = true;
        this.goalService.getStudentReport(studentId)
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
        this.meetingService.getStudentAttendanceSummary(studentId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (summary) => { this.attendanceSummary = summary; },
                error: () => { this.attendanceSummary = null; }
            });
    }

    applyFilter(): void {
        const name = this.nameFilter.toLowerCase().trim();
        this.filteredStudents = this.students.filter(s => {
            const fullName = `${s.firstName} ${s.lastName}`.toLowerCase();
            const matchName = !name || fullName.includes(name);
            const matchLevel = !this.levelFilter || s.educationLevel === this.levelFilter;
            return matchName && matchLevel;
        });
    }

    selectStudent(student: StudentSummary): void {
        if (this.selectedStudent?.studentId === student.studentId) return;
        this.selectedStudent = null;
        this.attendanceSummary = null;
        this.isLoadingReport = true;
        this.chartCategory = null;
        this.chartStartDate = '2000-01-01';
        this.chartEndDate = new Date().toISOString().split('T')[0];
        this.chartData = [];
        this.chartTotal = 0;

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

        this.meetingService.getStudentAttendanceSummary(student.studentId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (summary) => { this.attendanceSummary = summary; },
                error: () => { this.attendanceSummary = null; }
            });
    }

    approve(task: ReportTaskItem): void {
        if (!this.selectedStudent || !this.isMentorOrAdmin) return;
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
        if (!this.selectedStudent || !this.isMentorOrAdmin) return;
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

    setDateRange(period: 'week' | 'month' | '6months' | 'year' | 'all'): void {
        const today = new Date();
        this.chartEndDate = today.toISOString().split('T')[0];
        if (period === 'week') {
            const d = new Date(today); d.setDate(d.getDate() - 7);
            this.chartStartDate = d.toISOString().split('T')[0];
        } else if (period === 'month') {
            const d = new Date(today); d.setMonth(d.getMonth() - 1);
            this.chartStartDate = d.toISOString().split('T')[0];
        } else if (period === '6months') {
            const d = new Date(today); d.setMonth(d.getMonth() - 6);
            this.chartStartDate = d.toISOString().split('T')[0];
        } else if (period === 'year') {
            const d = new Date(today); d.setFullYear(d.getFullYear() - 1);
            this.chartStartDate = d.toISOString().split('T')[0];
        } else {
            this.chartStartDate = '2000-01-01';
        }
        this.loadChart();
    }

    getBarPx(value: number): number {
        return Math.max(Math.round((value / this.chartMaxValue) * this.CHART_BAR_PX), 3);
    }

    loadChart(): void {
        if (!this.selectedStudent || !this.chartCategory) {
            this.chartData = [];
            this.chartTotal = 0;
            return;
        }
        this.isLoadingChart = true;
        this.goalService.getCategoryChart(
            this.selectedStudent.studentId,
            this.chartCategory,
            this.chartStartDate || undefined,
            this.chartEndDate || undefined
        ).pipe(takeUntilDestroyed(this.destroyRef))
         .subscribe({
            next: (data) => {
                this.chartData = data;
                this.chartTotal = data.reduce((s, d) => s + d.value, 0);
                this.isLoadingChart = false;
            },
            error: () => { this.isLoadingChart = false; }
        });
    }

    getProgressColor(pct: number): string {
        if (pct >= 100) return '#4ade80';
        if (pct >= 70)  return '#60a5fa';
        if (pct >= 30)  return '#facc15';
        return '#f87171';
    }
}
