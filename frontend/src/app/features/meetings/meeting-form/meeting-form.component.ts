import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { MeetingService } from '../../../core/services/meeting.service';
import { ClassService } from '../../../core/services/class.service';
import { RoomService } from '../../../core/services/room.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ClassResponse } from '../../../core/models/class.model';
import { Room } from '../../../core/models/room.model';
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
    private roomService = inject(RoomService);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private notifications = inject(NotificationService);

    meetingForm: FormGroup;
    classes: ClassResponse[] = [];
    rooms: Room[] = [];
    isSubmitting = false;
    isLoading = false;
    isEditMode = false;
    meetingId: number | null = null;

    // When a class is selected show its assigned mentor
    get selectedClassMentor(): string {
        const id = this.meetingForm.get('classId')?.value;
        const cls = this.classes.find(c => c.id == id);
        return cls?.mentorName ?? '';
    }

    constructor() {
        this.meetingForm = this.fb.group({
            classId: ['', Validators.required],
            title: ['', [Validators.required, Validators.minLength(3)]],
            description: [''],
            startTime: ['', Validators.required],
            endTime: ['', Validators.required],
            location: [''],
            roomId: [null],
            recurring: [false]
        });
    }

    ngOnInit(): void {
        const idParam = this.route.snapshot.paramMap.get('id');
        if (idParam) {
            this.isEditMode = true;
            this.meetingId = Number(idParam);
        }

        this.isLoading = true;
        forkJoin({
            classes: this.classService.getClasses(),
            rooms: this.roomService.getAllRooms()
        }).subscribe({
            next: ({ classes, rooms }) => {
                this.classes = classes;
                this.rooms = rooms;
                if (this.isEditMode && this.meetingId) {
                    this.loadMeeting(this.meetingId);
                } else {
                    this.isLoading = false;
                }
            },
            error: () => { this.isLoading = false; }
        });
    }

    private loadMeeting(id: number): void {
        this.meetingService.getMeetingById(id).subscribe({
            next: (m) => {
                this.meetingForm.patchValue({
                    classId: m.classId,
                    title: m.title,
                    description: m.description,
                    startTime: this.toDatetimeLocal(m.startTime),
                    endTime: this.toDatetimeLocal(m.endTime),
                    location: m.location ?? '',
                    roomId: m.roomId ?? null,
                    recurring: m.recurring
                });
                // Disable classId in edit mode — class can't be changed
                this.meetingForm.get('classId')?.disable();
                this.meetingForm.get('recurring')?.disable();
                this.isLoading = false;
            },
            error: (err) => {
                this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to load meeting'));
                this.isLoading = false;
            }
        });
    }

    private toDatetimeLocal(iso: string): string {
        // Convert ISO string to datetime-local format (YYYY-MM-DDTHH:mm)
        return iso ? iso.substring(0, 16) : '';
    }

    onSubmit(): void {
        if (this.meetingForm.invalid) return;
        this.isSubmitting = true;

        const raw = this.meetingForm.getRawValue();

        if (this.isEditMode && this.meetingId) {
            const updatePayload = {
                title: raw.title,
                description: raw.description,
                startTime: raw.startTime,
                endTime: raw.endTime,
                location: raw.location || null,
                roomId: raw.roomId || null
            };
            this.meetingService.updateMeeting(this.meetingId, updatePayload).subscribe({
                next: () => { this.router.navigate(['/meetings']); },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to update meeting'));
                    this.isSubmitting = false;
                }
            });
        } else {
            const createPayload = {
                classId: Number(raw.classId),
                title: raw.title,
                description: raw.description,
                startTime: raw.startTime,
                endTime: raw.endTime,
                location: raw.location || null,
                roomId: raw.roomId || null,
                recurring: raw.recurring
            };
            this.meetingService.createMeeting(createPayload).subscribe({
                next: () => { this.router.navigate(['/meetings']); },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to schedule meeting'));
                    this.isSubmitting = false;
                }
            });
        }
    }
}
