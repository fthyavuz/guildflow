import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ClassService } from '../../../core/services/class.service';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { GoalService } from '../../../core/services/goal.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ClassResponse } from '../../../core/models/class.model';
import { User } from '../../../core/models/auth.model';
import { Observable, switchMap, forkJoin, BehaviorSubject, combineLatest, of, take } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { StudentProgressSummary } from '../../../core/models/student-progress.model';

@Component({
    selector: 'app-class-detail',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
    templateUrl: './class-detail.component.html',
    styleUrl: './class-detail.component.css'
})
export class ClassDetailComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private classService = inject(ClassService);
    private userService = inject(UserService);
    private authService = inject(AuthService);
    private goalService = inject(GoalService);
    private destroyRef = inject(DestroyRef);
    private notifications = inject(NotificationService);

    classId: number | null = null;
    user$ = this.authService.currentUser$;
    private refresh$ = new BehaviorSubject<void>(undefined);

    data$: Observable<{ class: ClassResponse; students: StudentProgressSummary[]; allStudents: User[] }> | undefined;
    searchTerm = '';
    pendingEnrollments: User[] = [];
    isSaving = false;

    // ── Tabs ──────────────────────────────────────────────────────────────────
    activeTab: 'roster' | 'assignments' = 'roster';

    // ── Assignments tab ───────────────────────────────────────────────────────
    assignments: any[] = [];
    assignmentsLoading = false;

    // ── Assign panel ──────────────────────────────────────────────────────────
    showAssignPanel = false;
    allTemplates: any[] = [];
    templateSearch = '';
    filteredTemplates: any[] = [];
    selectedTemplate: any = null;
    assignFrequency = '';
    assignStartDate = '';
    assignEndDate = '';
    assignApplyToAll = true;
    assignStudentIds: number[] = [];
    isAssigning = false;

    ngOnInit(): void {
        this.classId = Number(this.route.snapshot.paramMap.get('id'));

        this.data$ = this.refresh$.pipe(
            switchMap(() => this.user$.pipe(
                take(1),
                switchMap(user => combineLatest({
                    class: this.classService.getClassById(this.classId!),
                    students: this.classService.getClassProgressSummary(this.classId!),
                    allStudents: user?.role === 'ADMIN'
                        ? this.userService.getStudents()
                        : of([] as User[])
                }))
            ))
        );

        this.loadAssignments();
    }

    // ── Roster methods ────────────────────────────────────────────────────────

    stageStudent(student: User): void {
        if (!this.pendingEnrollments.find(s => s.id === student.id)) {
            this.pendingEnrollments.push(student);
        }
    }

    removeFromPending(studentId: number): void {
        this.pendingEnrollments = this.pendingEnrollments.filter(s => s.id !== studentId);
    }

    saveEnrollments(): void {
        if (!this.classId || this.pendingEnrollments.length === 0) return;
        this.isSaving = true;
        const requests = this.pendingEnrollments.map(s =>
            this.classService.addStudentToClass(this.classId!, s.id)
        );
        forkJoin(requests).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: () => {
                this.pendingEnrollments = [];
                this.isSaving = false;
                this.refresh$.next();
            },
            error: (err) => {
                this.notifications.error(this.notifications.extractErrorMessage(err, 'An error occurred while saving enrollments'));
                this.isSaving = false;
            }
        });
    }

    removeStudent(studentId: number): void {
        if (this.classId && confirm('Are you sure you want to remove this student from the class?')) {
            this.classService.removeStudentFromClass(this.classId, studentId)
                .pipe(takeUntilDestroyed(this.destroyRef))
                .subscribe({
                    next: () => this.refresh$.next(),
                    error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to remove student'))
                });
        }
    }

    getAvailableStudents(all: User[], enrolled: StudentProgressSummary[]): User[] {
        const enrolledIds = new Set(enrolled.map(s => s.studentId));
        const pendingIds = new Set(this.pendingEnrollments.map(s => s.id));
        return all.filter(s => !enrolledIds.has(s.id) && !pendingIds.has(s.id) &&
            (s.firstName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                s.lastName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                s.email.toLowerCase().includes(this.searchTerm.toLowerCase())));
    }

    scrollToEnroll(): void {
        document.getElementById('enrollment-panel')?.scrollIntoView({ behavior: 'smooth' });
    }

    // ── Assignment methods ────────────────────────────────────────────────────

    loadAssignments(): void {
        if (!this.classId) return;
        this.assignmentsLoading = true;
        this.goalService.getClassAssignments(this.classId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (data) => { this.assignments = data; this.assignmentsLoading = false; },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to load assignments'));
                    this.assignmentsLoading = false;
                }
            });
    }

    openAssignPanel(): void {
        this.showAssignPanel = true;
        this.selectedTemplate = null;
        this.templateSearch = '';
        this.assignFrequency = '';
        this.assignStartDate = '';
        this.assignEndDate = '';
        this.assignApplyToAll = true;
        this.assignStudentIds = [];
        this.goalService.getTemplates()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (t) => { this.allTemplates = t; this.filteredTemplates = t; }
            });
    }

    closeAssignPanel(): void {
        this.showAssignPanel = false;
    }

    filterTemplates(): void {
        const q = this.templateSearch.toLowerCase().trim();
        this.filteredTemplates = q
            ? this.allTemplates.filter(t => t.title.toLowerCase().includes(q))
            : this.allTemplates;
    }

    selectTemplate(template: any): void {
        this.selectedTemplate = template;
    }

    toggleAssignStudent(studentId: number): void {
        const idx = this.assignStudentIds.indexOf(studentId);
        if (idx === -1) this.assignStudentIds.push(studentId);
        else this.assignStudentIds.splice(idx, 1);
    }

    submitAssignment(): void {
        if (!this.classId || !this.selectedTemplate) return;
        this.isAssigning = true;
        const request: any = {
            goalId: this.selectedTemplate.id,
            frequency: this.assignFrequency || null,
            startDate: this.assignStartDate || null,
            endDate: this.assignEndDate || null,
            applyToAll: this.assignApplyToAll,
            studentIds: this.assignApplyToAll ? [] : this.assignStudentIds
        };
        this.goalService.createAssignment(this.classId, request)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (created) => {
                    this.assignments.unshift(created);
                    this.isAssigning = false;
                    this.showAssignPanel = false;
                    this.notifications.success('Homework assigned successfully');
                },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to assign homework'));
                    this.isAssigning = false;
                }
            });
    }

    removeAssignment(assignmentId: number): void {
        if (!this.classId || !confirm('Remove this homework assignment?')) return;
        this.goalService.deleteAssignment(this.classId, assignmentId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => { this.assignments = this.assignments.filter(a => a.id !== assignmentId); },
                error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to remove assignment'))
            });
    }

    // ── Preview modal ─────────────────────────────────────────────────────────

    previewAssignment: any = null;
    previewTemplate: any = null;
    previewLoading = false;
    previewInputs: { [taskId: number]: number | boolean } = {};

    openPreview(assignment: any): void {
        this.previewAssignment = assignment;
        this.previewTemplate = null;
        this.previewInputs = {};
        this.previewLoading = true;
        this.goalService.getGoalById(assignment.goalId)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (goal) => {
                    this.previewTemplate = goal;
                    for (const task of goal.tasks ?? []) {
                        this.previewInputs[task.id] = task.taskType === 'CHECKBOX' ? false : 0;
                    }
                    this.previewLoading = false;
                },
                error: () => { this.previewLoading = false; }
            });
    }

    closePreview(): void {
        this.previewAssignment = null;
        this.previewTemplate = null;
        this.previewInputs = {};
    }

    previewNumberInput(taskId: number): number {
        return (this.previewInputs[taskId] as number) ?? 0;
    }

    previewCheckboxInput(taskId: number): boolean {
        return (this.previewInputs[taskId] as boolean) ?? false;
    }

    setPreviewNumber(taskId: number, value: string): void {
        this.previewInputs[taskId] = Math.max(0, Number(value) || 0);
    }

    togglePreviewCheckbox(taskId: number): void {
        this.previewInputs[taskId] = !(this.previewInputs[taskId] as boolean);
    }

    get previewNumberTasks(): any[] {
        return (this.previewTemplate?.tasks ?? []).filter((t: any) => t.taskType === 'NUMBER');
    }

    get previewCheckboxTasks(): any[] {
        return (this.previewTemplate?.tasks ?? []).filter((t: any) => t.taskType === 'CHECKBOX');
    }

    get previewNumberTotal(): number {
        return this.previewNumberTasks.reduce((sum: number, t: any) => sum + (this.previewNumberInput(t.id)), 0);
    }

    get previewNumberTargetTotal(): number {
        return this.previewNumberTasks.reduce((sum: number, t: any) => sum + (t.targetValue ?? 0), 0);
    }

    get previewCheckedCount(): number {
        return this.previewCheckboxTasks.filter((t: any) => this.previewCheckboxInput(t.id)).length;
    }
}
