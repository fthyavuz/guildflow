import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MeetingService } from '../../../core/services/meeting.service';
import { ClassService } from '../../../core/services/class.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ClassResponse } from '../../../core/models/class.model';
import { Observable } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-meeting-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule, TranslateModule],
    templateUrl: './meeting-form.component.html',
    styleUrl: './meeting-form.component.css'
})
export class MeetingFormComponent implements OnInit {
    private fb = inject(FormBuilder);
    private meetingService = inject(MeetingService);
    private classService = inject(ClassService);
    private router = inject(Router);
    private notifications = inject(NotificationService);

    meetingForm: FormGroup;
    classes$: Observable<ClassResponse[]> | undefined;
    isSubmitting = false;

    constructor() {
        this.meetingForm = this.fb.group({
            classId: ['', Validators.required],
            title: ['', [Validators.required, Validators.minLength(3)]],
            description: [''],
            startTime: ['', Validators.required],
            endTime: ['', Validators.required],
            location: ['', Validators.required],
            recurring: [false]
        });
    }

    ngOnInit(): void {
        this.classes$ = this.classService.getClasses();
    }

    onSubmit(): void {
        if (this.meetingForm.valid) {
            this.isSubmitting = true;
            this.meetingService.createMeeting(this.meetingForm.value).subscribe({
                next: () => {
                    this.router.navigate(['/meetings']);
                },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to create meeting'));
                    this.isSubmitting = false;
                }
            });
        }
    }
}
