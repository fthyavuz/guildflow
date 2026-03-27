import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, FormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { GoalService } from '../../../core/services/goal.service';
import { ClassService } from '../../../core/services/class.service';
import { SourceService } from '../../../core/services/source.service';
import { NotificationService } from '../../../core/services/notification.service';
import { User } from '../../../core/models/auth.model';
import { Source } from '../../../core/models/source.model';
import { Observable } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-goal-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterModule, TranslateModule],
    templateUrl: './goal-form.component.html',
    styleUrl: './goal-form.component.css'
})
export class GoalFormComponent implements OnInit {
    private fb = inject(FormBuilder);
    private goalService = inject(GoalService);
    private classService = inject(ClassService);
    private sourceService = inject(SourceService);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private notifications = inject(NotificationService);

    goalForm: FormGroup;
    classId: number | null = null;
    isTemplate = false;
    students$: Observable<User[]> | undefined;

    allSources: Source[] = [];
    filteredSources: Source[] = [];
    sourceSearchQuery = '';

    isSubmitting = false;
    goalId: number | null = null;
    isEditMode = false;

    constructor() {
        this.goalForm = this.fb.group({
            title: ['', [Validators.required, Validators.minLength(3)]],
            description: [''],
            applyToAll: [true],
            isTemplate: [false],
            startDate: [''],
            endDate: [''],
            frequency: [null],
            studentIds: [[]],
            tasks: this.fb.array([])
        });
    }

    get tasks(): FormArray {
        return this.goalForm.get('tasks') as FormArray;
    }

    ngOnInit(): void {
        const idParam = this.route.snapshot.paramMap.get('id');
        this.goalId = idParam ? Number(idParam) : null;
        this.isEditMode = !!this.goalId;

        const classIdParam = this.route.snapshot.queryParamMap.get('classId');
        this.classId = classIdParam ? Number(classIdParam) : null;

        const templateParam = this.route.snapshot.queryParamMap.get('template');
        this.isTemplate = templateParam === 'true';

        this.sourceService.getAllSources().subscribe({
            next: (sources) => {
                this.allSources = sources;
                this.filteredSources = sources;
            }
        });

        if (this.isEditMode && this.goalId) {
            this.loadGoalData(this.goalId);
        } else {
            if (this.isTemplate) {
                this.goalForm.patchValue({ isTemplate: true });
                this.goalForm.get('startDate')?.clearValidators();
                this.goalForm.get('endDate')?.clearValidators();
            } else {
                this.goalForm.get('startDate')?.setValidators([Validators.required]);
                this.goalForm.get('endDate')?.setValidators([Validators.required]);

                if (!this.classId) {
                    this.router.navigate(['/classes']);
                    return;
                }
            }

            this.goalForm.get('startDate')?.updateValueAndValidity();
            this.goalForm.get('endDate')?.updateValueAndValidity();

            if (this.classId) {
                this.students$ = this.classService.getClassStudents(this.classId);
            }
        }
    }

    loadGoalData(id: number): void {
        this.goalService.getGoalById(id).subscribe({
            next: (goal) => {
                this.isTemplate = goal.isTemplate;
                this.classId = goal.classId;

                if (this.isTemplate) {
                    this.goalForm.get('startDate')?.clearValidators();
                    this.goalForm.get('endDate')?.clearValidators();
                } else {
                    this.goalForm.get('startDate')?.setValidators([Validators.required]);
                    this.goalForm.get('endDate')?.setValidators([Validators.required]);
                    if (this.classId) {
                        this.students$ = this.classService.getClassStudents(this.classId);
                    }
                }

                this.goalForm.patchValue({
                    title: goal.title,
                    description: goal.description,
                    applyToAll: goal.applyToAll,
                    isTemplate: goal.isTemplate,
                    startDate: goal.startDate ? goal.startDate.split('T')[0] : '',
                    endDate: goal.endDate ? goal.endDate.split('T')[0] : '',
                    frequency: goal.frequency ?? null,
                    studentIds: []
                });

                const taskArray = this.goalForm.get('tasks') as FormArray;
                taskArray.clear();
                goal.tasks.forEach((t: any) => {
                    taskArray.push(this.fb.group({
                        title: [t.title, Validators.required],
                        description: [t.description],
                        taskType: [t.taskType, Validators.required],
                        targetValue: [t.targetValue, [Validators.required, Validators.min(1)]],
                        sourceId: [t.source ? t.source.id : null],
                        sortOrder: [t.sortOrder]
                    }));
                });

                this.goalForm.get('startDate')?.updateValueAndValidity();
                this.goalForm.get('endDate')?.updateValueAndValidity();
            },
            error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to load goal'))
        });
    }

    filterSources(): void {
        const q = this.sourceSearchQuery.toLowerCase().trim();
        this.filteredSources = q
            ? this.allSources.filter(s => s.title.toLowerCase().includes(q))
            : this.allSources;
    }

    isSourceAdded(sourceId: number): boolean {
        return this.tasks.controls.some(c => c.get('sourceId')?.value === sourceId);
    }

    addSourceAsTask(source: Source): void {
        if (this.isSourceAdded(source.id)) return;
        const taskType = source.trackingType === 'LINEAR' ? 'NUMBER' : 'CHECKBOX';
        const targetValue = source.totalCapacity ?? 1;
        this.tasks.push(this.fb.group({
            title: [source.title, Validators.required],
            description: [''],
            taskType: [taskType, Validators.required],
            targetValue: [targetValue, [Validators.required, Validators.min(1)]],
            sourceId: [source.id],
            sortOrder: [this.tasks.length]
        }));
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

            const request = this.isEditMode && this.goalId
                ? this.goalService.updateGoal(this.goalId, data)
                : this.goalService.createGoal(data);

            request.subscribe({
                next: () => {
                    if (this.isTemplate) {
                        this.router.navigate(['/goals/library']);
                    } else if (this.classId) {
                        this.router.navigate(['/classes', this.classId]);
                    } else {
                        this.router.navigate(['/dashboard']);
                    }
                },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to save goal'));
                    this.isSubmitting = false;
                }
            });
        }
    }
}
