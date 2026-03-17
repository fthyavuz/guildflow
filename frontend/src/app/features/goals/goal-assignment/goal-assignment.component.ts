import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { GoalService } from '../../../core/services/goal.service';
import { ClassService } from '../../../core/services/class.service';
import { TranslateModule } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { User } from '../../../core/models/auth.model';

@Component({
  selector: 'app-goal-assignment',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, TranslateModule],
  templateUrl: './goal-assignment.component.html',
  styleUrl: './goal-assignment.component.css'
})
export class GoalAssignmentComponent implements OnInit {
  private fb = inject(FormBuilder);
  private goalService = inject(GoalService);
  private classService = inject(ClassService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  assignForm: FormGroup;
  templateId: number | null = null;
  template: any = null;
  
  classes$: Observable<any[]> | undefined;
  students$: Observable<User[]> | undefined;
  isSubmitting = false;

  constructor() {
    this.assignForm = this.fb.group({
      classId: [null, Validators.required],
      applyToAll: [true],
      studentIds: [[]],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.templateId = id ? Number(id) : null;

    if (!this.templateId) {
      this.router.navigate(['/goals/library']);
      return;
    }

    // Load template details (we might want a specific endpoint or just get all and filter)
    this.goalService.getTemplates().subscribe(templates => {
      this.template = templates.find(t => t.id === this.templateId);
      if (!this.template) {
        this.router.navigate(['/goals/library']);
      }
    });

    this.classes$ = this.classService.getMentorClasses();

    this.assignForm.get('classId')?.valueChanges.subscribe(classId => {
      if (classId) {
        this.students$ = this.classService.getClassStudents(classId);
        this.assignForm.patchValue({ studentIds: [] });
      }
    });
  }

  toggleStudentSelection(studentId: number, event: any): void {
    const currentIds = this.assignForm.get('studentIds')?.value as number[];
    if (event.target.checked) {
      this.assignForm.patchValue({ studentIds: [...currentIds, studentId] });
    } else {
      this.assignForm.patchValue({ studentIds: currentIds.filter(id => id !== studentId) });
    }
  }

  onSubmit(): void {
    if (this.assignForm.valid) {
      this.isSubmitting = true;
      const data = {
        ...this.assignForm.value,
        goalId: this.templateId
      };

      this.goalService.assignTemplate(data).subscribe({
        next: () => {
          this.router.navigate(['/goals/library']);
        },
        error: (err) => {
          console.error('Error assigning goal:', err);
          this.isSubmitting = false;
        }
      });
    }
  }
}
