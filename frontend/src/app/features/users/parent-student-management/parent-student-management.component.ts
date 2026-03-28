import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TranslateModule } from '@ngx-translate/core';
import { UserService } from '../../../core/services/user.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UserResponse } from '../../../core/models/auth.model';

@Component({
    selector: 'app-parent-student-management',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
    templateUrl: './parent-student-management.component.html',
    styleUrl: './parent-student-management.component.css'
})
export class ParentStudentManagementComponent implements OnInit {
    private userService = inject(UserService);
    private notifications = inject(NotificationService);
    private destroyRef = inject(DestroyRef);

    activeTab: 'parents' | 'students' = 'parents';

    parents: UserResponse[] = [];
    students: UserResponse[] = [];
    isLoadingParents = true;
    isLoadingStudents = true;

    parentSearch = '';
    studentSearch = '';

    // Link parent modal
    linkModal = false;
    linkingStudent: UserResponse | null = null;
    selectedParentId: number | null = null;
    isLinking = false;

    ngOnInit(): void {
        this.loadParents();
        this.loadStudents();
    }

    loadParents(): void {
        this.isLoadingParents = true;
        this.userService.getParentsList()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (list) => { this.parents = list; this.isLoadingParents = false; },
                error: () => { this.isLoadingParents = false; }
            });
    }

    loadStudents(): void {
        this.isLoadingStudents = true;
        this.userService.getStudentsList()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: (list) => { this.students = list; this.isLoadingStudents = false; },
                error: () => { this.isLoadingStudents = false; }
            });
    }

    get filteredParents(): UserResponse[] {
        const q = this.parentSearch.toLowerCase();
        if (!q) return this.parents;
        return this.parents.filter(p =>
            `${p.firstName} ${p.lastName}`.toLowerCase().includes(q) ||
            p.email.toLowerCase().includes(q)
        );
    }

    get filteredStudents(): UserResponse[] {
        const q = this.studentSearch.toLowerCase();
        if (!q) return this.students;
        return this.students.filter(s =>
            `${s.firstName} ${s.lastName}`.toLowerCase().includes(q) ||
            s.email.toLowerCase().includes(q) ||
            (s.parentName?.toLowerCase().includes(q) ?? false)
        );
    }

    studentsOfParent(parentId: number): UserResponse[] {
        return this.students.filter(s => s.parentId === parentId);
    }

    initials(user: UserResponse): string {
        return `${user.firstName[0]}${user.lastName[0]}`.toUpperCase();
    }

    // ── Link parent modal ──────────────────────────────

    openLinkModal(student: UserResponse): void {
        this.linkingStudent = student;
        this.selectedParentId = student.parentId ?? null;
        this.linkModal = true;
    }

    closeLinkModal(): void {
        this.linkModal = false;
        this.linkingStudent = null;
        this.selectedParentId = null;
    }

    submitLink(): void {
        if (!this.linkingStudent || !this.selectedParentId || this.isLinking) return;
        this.isLinking = true;
        const studentId = this.linkingStudent.id;
        const newParentId = Number(this.selectedParentId);

        const doLink = () => {
            this.userService.linkParentToStudent(newParentId, studentId)
                .pipe(takeUntilDestroyed(this.destroyRef))
                .subscribe({
                    next: () => {
                        this.notifications.success('Parent linked successfully');
                        this.isLinking = false;
                        this.closeLinkModal();
                        this.loadParents();
                        this.loadStudents();
                    },
                    error: (err) => {
                        this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to link parent'));
                        this.isLinking = false;
                    }
                });
        };

        // Unlink old parent first if student already has one
        if (this.linkingStudent.parentId && this.linkingStudent.parentId !== newParentId) {
            this.userService.unlinkParentFromStudent(this.linkingStudent.parentId, studentId)
                .pipe(takeUntilDestroyed(this.destroyRef))
                .subscribe({ next: doLink, error: doLink });
        } else {
            doLink();
        }
    }

    unlinkParent(student: UserResponse): void {
        if (!student.parentId) return;
        this.userService.unlinkParentFromStudent(student.parentId, student.id)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: () => {
                    this.notifications.success('Parent unlinked');
                    this.loadParents();
                    this.loadStudents();
                },
                error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to unlink'))
            });
    }
}
