import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ClassService } from '../../../core/services/class.service';
import { StudentProfile } from '../../../core/models/student.model';
import { Observable, switchMap, map } from 'rxjs';
import { Location } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-student-profile',
    standalone: true,
    imports: [CommonModule, RouterModule, TranslateModule],
    templateUrl: './student-profile.component.html',
    styleUrl: './student-profile.component.css'
})
export class StudentProfileComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private classService = inject(ClassService);
    private location = inject(Location);

    profile$: Observable<StudentProfile> | undefined;

    ngOnInit(): void {
        this.profile$ = this.route.paramMap.pipe(
            map(params => Number(params.get('studentId'))),
            switchMap(id => this.classService.getStudentProfile(id))
        );
    }

    getProgressColor(percentage: number): string {
        if (percentage >= 100) return '#4ade80'; // Success green
        if (percentage >= 70) return '#60a5fa'; // Primary blue
        if (percentage >= 30) return '#facc15'; // Warning yellow
        return '#f87171'; // Critical red
    }

    back(): void {
        this.location.back();
    }
}
