import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { GoalService } from '../../../core/services/goal.service';
import { ClassService } from '../../../core/services/class.service';
import { User } from '../../../core/models/auth.model';
import { Observable } from 'rxjs';

@Component({
    selector: 'app-goal-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './goal-form.component.html',
    styleUrl: './goal-form.component.css'
})
export class GoalFormComponent implements OnInit {
    private fb = inject(FormBuilder);
    private goalService = inject(GoalService);
    private classService = inject(ClassService);
    private router = inject(Router);
    private route = inject(ActivatedRoute);

    goalForm: FormGroup;
    classId: number | null = null;
    goalTypes$: Observable<any[]> | undefined;
    students$: Observable<User[]> | undefined;
    isSubmitting = false;

    constructor() {
        this.goalForm = this.fb.group({
            title: ['', [Validators.required, Validators.minLength(3)]],
            description: [''],
            goalTypeId: [null, Validators.required],
            applyToAll: [true],
            startDate: ['', Validators.required],
            endDate: ['', Validators.required],
            studentIds: [[]],
            tasks: this.fb.array([])
        });
    }

    get tasks(): FormArray {
        return this.goalForm.get('tasks') as FormArray;
    }

    ngOnInit(): void {
        const id = this.route.snapshot.queryParamMap.get('classId');
        this.classId = id ? Number(id) : null;

        if (!this.classId) {
            this.router.navigate(['/classes']);
            return;
        }

        this.goalTypes$ = this.goalService.getGoalTypes();
        this.students$ = this.classService.getClassStudents(this.classId);

        // Add one initial task
        this.addTask();
    }

    addTask(): void {
        const taskForm = this.fb.group({
            title: ['', Validators.required],
            description: [''],
            taskType: ['NUMBER', Validators.required],
            targetValue: [1, [Validators.required, Validators.min(1)]],
            sortOrder: [this.tasks.length]
        });
        this.tasks.push(taskForm);
    }

    removeTask(index: number): void {
        this.tasks.removeAt(index);
    }

    toggleStudentSelection(studentId: number, event: any): void {
        const currentIds = this.goalForm.get('studentIds')?.value as number[];
        if (event.target.checked) {
            this.goalForm.patchValue({ studentIds: [...currentIds, studentId] });
        } else {
            this.goalForm.patchValue({ studentIds: currentIds.filter(id => id !== studentId) });
        }
    }

    onSubmit(): void {
        if (this.goalForm.valid && this.tasks.length > 0) {
            this.isSubmitting = true;
            const data = {
                ...this.goalForm.value,
                classId: this.classId
            };

            this.goalService.createGoal(data).subscribe({
                next: () => {
                    this.router.navigate(['/classes', this.classId]);
                },
                error: (err) => {
                    console.error('Error creating goal:', err);
                    this.isSubmitting = false;
                }
            });
        }
    }
}
