import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ClassService } from '../../../core/services/class.service';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ClassResponse } from '../../../core/models/class.model';
import { User } from '../../../core/models/auth.model';
import { Observable, switchMap, forkJoin, BehaviorSubject, combineLatest } from 'rxjs';
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
    private destroyRef = inject(DestroyRef);
    private notifications = inject(NotificationService);

    classId: number | null = null;
    user$ = this.authService.currentUser$;
    private refresh$ = new BehaviorSubject<void>(undefined);

    data$: Observable<{ class: ClassResponse; students: StudentProgressSummary[]; allStudents: User[] }> | undefined;
    searchTerm: string = '';
    pendingEnrollments: User[] = [];
    isSaving: boolean = false;

    ngOnInit(): void {
        this.classId = Number(this.route.snapshot.paramMap.get('id'));

        this.data$ = this.refresh$.pipe(
            switchMap(() => combineLatest({
                class: this.classService.getClassById(this.classId!),
                students: this.classService.getClassProgressSummary(this.classId!),
                allStudents: this.userService.getStudents()
            }))
        );
    }

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

    addStudent(studentId: number): void {
        if (this.classId) {
            this.classService.addStudentToClass(this.classId, studentId)
                .pipe(takeUntilDestroyed(this.destroyRef))
                .subscribe({
                    next: () => this.refresh$.next(),
                    error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to add student'))
                });
        }
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
}
