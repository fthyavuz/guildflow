import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { TranslateModule } from '@ngx-translate/core';
import { UserService } from '../../../core/services/user.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UserResponse } from '../../../core/models/auth.model';
import { PagedResponse } from '../../../core/models/page.model';

type RoleFilter = '' | 'ADMIN' | 'MENTOR' | 'STUDENT' | 'PARENT';

@Component({
    selector: 'app-user-management',
    standalone: true,
    imports: [CommonModule, RouterModule, ReactiveFormsModule, TranslateModule],
    templateUrl: './user-management.component.html',
    styleUrl: './user-management.component.css'
})
export class UserManagementComponent implements OnInit, OnDestroy {
    private userService = inject(UserService);
    private notifications = inject(NotificationService);
    private fb = inject(FormBuilder);
    private destroy$ = new Subject<void>();

    users: UserResponse[] = [];
    totalElements = 0;
    totalPages = 0;
    currentPage = 0;
    pageSize = 20;
    isLoading = false;

    selectedRole: RoleFilter = '';
    searchTerm = '';
    private searchSubject = new Subject<string>();

    roles: RoleFilter[] = ['', 'ADMIN', 'MENTOR', 'STUDENT', 'PARENT'];

    // Reset password modal state
    resetModal = false;
    targetUser: UserResponse | null = null;
    resetForm: FormGroup;
    isResetting = false;

    constructor() {
        this.resetForm = this.fb.group({
            newPassword: ['', [Validators.required, Validators.minLength(6)]],
            confirmPassword: ['', [Validators.required]]
        }, { validators: this.passwordsMatch });
    }

    ngOnInit(): void {
        this.searchSubject.pipe(
            debounceTime(350),
            distinctUntilChanged(),
            takeUntil(this.destroy$)
        ).subscribe(term => {
            this.searchTerm = term;
            this.currentPage = 0;
            this.loadUsers();
        });

        this.loadUsers();
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    loadUsers(): void {
        this.isLoading = true;
        this.userService.getUsersPage({
            role: this.selectedRole || undefined,
            search: this.searchTerm || undefined,
            page: this.currentPage,
            size: this.pageSize
        }).pipe(takeUntil(this.destroy$)).subscribe({
            next: (res: PagedResponse<UserResponse>) => {
                this.users = res.content;
                this.totalElements = res.totalElements;
                this.totalPages = res.totalPages;
                this.isLoading = false;
            },
            error: (err) => {
                this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to load users'));
                this.isLoading = false;
            }
        });
    }

    onSearch(event: Event): void {
        const value = (event.target as HTMLInputElement).value;
        this.searchSubject.next(value);
    }

    onRoleChange(role: RoleFilter): void {
        this.selectedRole = role;
        this.currentPage = 0;
        this.loadUsers();
    }

    goToPage(page: number): void {
        if (page < 0 || page >= this.totalPages) return;
        this.currentPage = page;
        this.loadUsers();
    }

    // ── Reset Password ─────────────────────────────────
    openResetModal(user: UserResponse): void {
        this.targetUser = user;
        this.resetForm.reset();
        this.resetModal = true;
    }

    closeResetModal(): void {
        this.resetModal = false;
        this.targetUser = null;
    }

    submitReset(): void {
        if (this.resetForm.invalid || !this.targetUser) return;
        this.isResetting = true;
        const { newPassword } = this.resetForm.value;

        this.userService.adminResetPassword(this.targetUser.id, newPassword).subscribe({
            next: () => {
                this.notifications.success(`Password reset for ${this.targetUser!.firstName} ${this.targetUser!.lastName}`);
                this.closeResetModal();
                this.isResetting = false;
            },
            error: (err) => {
                this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to reset password'));
                this.isResetting = false;
            }
        });
    }

    private passwordsMatch(group: FormGroup) {
        const pw = group.get('newPassword')?.value;
        const confirm = group.get('confirmPassword')?.value;
        return pw === confirm ? null : { mismatch: true };
    }

    roleBadgeClass(role: string): string {
        const map: Record<string, string> = {
            ADMIN: 'badge-admin',
            MENTOR: 'badge-mentor',
            STUDENT: 'badge-student',
            PARENT: 'badge-parent'
        };
        return map[role] ?? '';
    }

    get pages(): number[] {
        return Array.from({ length: this.totalPages }, (_, i) => i);
    }
}
