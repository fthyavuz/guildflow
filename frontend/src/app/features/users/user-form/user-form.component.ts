import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
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

  userForm: FormGroup;
  userType: 'MENTOR' | 'STUDENT' = 'MENTOR';
  isEditMode = false;
  userId: number | null = null;
  isSubmitting = false;

  constructor() {
    this.userForm = this.fb.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      phone: ['']
    });
  }

  ngOnInit(): void {
    const segments = this.route.snapshot.url;
    this.userType = segments[0]?.path === 'mentors' ? 'MENTOR' : 'STUDENT';

    this.userId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.userId) {
      this.isEditMode = true;
      this.userForm.get('password')?.clearValidators();
      this.userForm.get('password')?.updateValueAndValidity();
      this.loadUser(this.userId);
    }
  }

  loadUser(id: number): void {
    this.userService.getUserById(id).subscribe({
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

  onSubmit(): void {
    if (this.userForm.valid) {
      this.isSubmitting = true;
      const userData = {
        ...this.userForm.value,
        role: this.userType
      };

      const obs$ = this.isEditMode
        ? this.userService.updateUser(this.userId!, userData)
        : this.userService.createUser(userData);

      obs$.subscribe({
        next: () => {
          this.router.navigate([this.userType === 'MENTOR' ? '/mentors' : '/students']);
        },
        error: (err) => {
          this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to save user'));
          this.isSubmitting = false;
        }
      });
    }
  }
}
