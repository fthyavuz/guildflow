import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { UserService } from '../../../core/services/user.service';
import { UserResponse } from '../../../core/models/auth.model';
import { NotificationService } from '../../../core/services/notification.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, TranslateModule],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.css'
})
export class UserFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private notifications = inject(NotificationService);
  private destroyRef = inject(DestroyRef);

  userForm: FormGroup;
  userType: 'MENTOR' | 'STUDENT' | 'PARENT' = 'MENTOR';
  isEditMode = false;
  userId: number | null = null;
  isSubmitting = false;

  parents: UserResponse[] = [];
  isLoadingParents = false;

  constructor() {
    this.userForm = this.fb.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      phone: [''],
      parentId: [null]
    });
  }

  ngOnInit(): void {
    const segments = this.route.snapshot.url;
    const firstPath = segments[0]?.path;
    if (firstPath === 'mentors') this.userType = 'MENTOR';
    else if (firstPath === 'parents') this.userType = 'PARENT';
    else if (firstPath === 'admins') this.userType = 'ADMIN' as any;
    else this.userType = 'STUDENT';

    this.userId = Number(this.route.snapshot.paramMap.get('id')) || null;
    if (this.userId) {
      this.isEditMode = true;
      this.userForm.get('password')?.clearValidators();
      this.userForm.get('password')?.updateValueAndValidity();
      this.loadUser(this.userId);
    }

    // Students must have a parent when creating
    if (this.userType === 'STUDENT' && !this.isEditMode) {
      this.userForm.get('parentId')?.setValidators(Validators.required);
      this.userForm.get('parentId')?.updateValueAndValidity();
      this.loadParents();
    }
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

  loadUser(id: number): void {
    this.userService.getUserById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (user) => {
          this.userForm.patchValue({
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email,
            phone: user.phone
          });
        },
        error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to load user'))
      });
  }

  get backRoute(): string {
    if (this.userType === 'MENTOR') return '/mentors';
    if ((this.userType as string) === 'ADMIN') return '/users';
    return '/students';
  }

  onSubmit(): void {
    if (this.userForm.invalid || this.isSubmitting) return;
    this.isSubmitting = true;

    const formVal = this.userForm.value;
    const userData: any = {
      firstName: formVal.firstName,
      lastName: formVal.lastName,
      email: formVal.email,
      phone: formVal.phone,
      role: this.userType
    };
    if (formVal.password) userData.password = formVal.password;
    if (this.userType === 'STUDENT' && !this.isEditMode && formVal.parentId) {
      userData.parentId = Number(formVal.parentId);
    }

    const obs$ = this.isEditMode
      ? this.userService.updateUser(this.userId!, userData)
      : this.userService.createUser(userData);

    obs$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => { this.router.navigate([this.backRoute]); },
      error: (err) => {
        this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to save user'));
        this.isSubmitting = false;
      }
    });
  }
}
