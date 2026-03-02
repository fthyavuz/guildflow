import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ClassService } from '../../../core/services/class.service';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { ClassResponse } from '../../../core/models/class.model';
import { User } from '../../../core/models/auth.model';
import { Observable, switchMap, forkJoin, map, BehaviorSubject, of, combineLatest } from 'rxjs';
import { FormsModule } from '@angular/forms';

import { StudentProgressSummary } from '../../../core/models/student-progress.model';

@Component({
    selector: 'app-class-detail',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule],
    templateUrl: './class-detail.component.html',
    styleUrl: './class-detail.component.css'
})
export class ClassDetailComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private classService = inject(ClassService);
    private userService = inject(UserService);
    private authService = inject(AuthService);

    classId: number | null = null;
    user$ = this.authService.currentUser$;
    private refresh$ = new BehaviorSubject<void>(undefined);

    data$: Observable<{ class: ClassResponse; students: StudentProgressSummary[]; allStudents: User[] }> | undefined;
    searchTerm: string = '';

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

    addStudent(studentId: number): void {
        if (this.classId) {
            this.classService.addStudentToClass(this.classId, studentId).subscribe({
                next: () => this.refresh$.next(),
                error: (err) => console.error('Error adding student:', err)
            });
        }
    }

    removeStudent(studentId: number): void {
        if (this.classId && confirm('Are you sure you want to remove this scholar from the guild?')) {
            this.classService.removeStudentFromClass(this.classId, studentId).subscribe({
                next: () => this.refresh$.next(),
                error: (err) => console.error('Error removing student:', err)
            });
        }
    }

    getAvailableStudents(all: User[], enrolled: StudentProgressSummary[]): User[] {
        const enrolledIds = new Set(enrolled.map(s => s.studentId));
        return all.filter(s => !enrolledIds.has(s.id) &&
            (s.firstName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                s.lastName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                s.email.toLowerCase().includes(this.searchTerm.toLowerCase())));
    }
}
