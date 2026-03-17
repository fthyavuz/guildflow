import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { ClassService } from '../../../core/services/class.service';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserResponse } from '../../../core/models/auth.model';
import { Observable, of } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-class-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule, TranslateModule],
    templateUrl: './class-form.component.html',
    styleUrl: './class-form.component.css'
})
export class ClassFormComponent implements OnInit {
    private fb = inject(FormBuilder);
    private classService = inject(ClassService);
    private userService = inject(UserService);
    private authService = inject(AuthService);
    private router = inject(Router);
    private route = inject(ActivatedRoute);

    classForm: FormGroup;
    mentors$: Observable<UserResponse[]> | undefined;
    isEditMode = false;
    classId: number | null = null;
    isSubmitting = false;
    currentUserRole: string | undefined;

    educationLevels = ['PRIMARY', 'SECONDARY', 'HIGH_SCHOOL', 'UNIVERSITY', 'ADULT'];

    constructor() {
        this.classForm = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(3)]],
            description: [''],
            educationLevel: ['', Validators.required],
            mentorId: [null]
        });
    }

    ngOnInit(): void {
        this.authService.currentUser$.subscribe(user => {
            this.currentUserRole = user?.role;
            if (this.currentUserRole === 'ADMIN') {
                this.mentors$ = this.userService.getMentors();
                this.classForm.get('mentorId')?.setValidators(Validators.required);
            } else {
                this.classForm.get('mentorId')?.clearValidators();
            }
            this.classForm.get('mentorId')?.updateValueAndValidity();
        });

        this.classId = Number(this.route.snapshot.paramMap.get('id'));
        if (this.classId) {
            this.isEditMode = true;
            this.loadClassData(this.classId);
        }
    }

    loadClassData(id: number): void {
        this.classService.getClassById(id).subscribe({
            next: (cls) => {
                this.classForm.patchValue({
                    name: cls.name,
                    description: cls.description,
                    educationLevel: cls.educationLevel,
                    mentorId: cls.mentorId
                });
            },
            error: (err) => console.error('Error loading class:', err)
        });
    }

    onSubmit(): void {
        if (this.classForm.valid) {
            this.isSubmitting = true;
            const classData = this.classForm.value;

            const obs$ = this.isEditMode
                ? this.classService.updateClass(this.classId!, classData)
                : this.classService.createClass(classData);

            obs$.subscribe({
                next: () => {
                    this.router.navigate(['/classes']);
                },
                error: (err) => {
                    console.error('Error saving class:', err);
                    this.isSubmitting = false;
                }
            });
        }
    }
}
