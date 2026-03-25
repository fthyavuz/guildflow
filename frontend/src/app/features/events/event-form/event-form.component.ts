import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EventService } from '../../../core/services/event.service';
import { ClassService } from '../../../core/services/class.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ClassResponse } from '../../../core/models/class.model';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-event-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule, TranslateModule],
    templateUrl: './event-form.component.html',
    styleUrl: './event-form.component.css'
})
export class EventFormComponent implements OnInit {
    private fb = inject(FormBuilder);
    private eventService = inject(EventService);
    private classService = inject(ClassService);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private notifications = inject(NotificationService);

    eventForm: FormGroup;
    isEditMode = false;
    eventId: number | null = null;
    isSubmitting = false;
    classes: ClassResponse[] = [];

    readonly educationLevels = [
        { value: '', label: 'All levels (no restriction)' },
        { value: 'PRIMARY', label: 'Primary' },
        { value: 'SECONDARY', label: 'Secondary' },
        { value: 'HIGH_SCHOOL', label: 'High School' },
        { value: 'UNIVERSITY', label: 'University' }
    ];

    constructor() {
        this.eventForm = this.fb.group({
            title: ['', [Validators.required]],
            description: [''],
            startTime: ['', [Validators.required]],
            endTime: ['', [Validators.required]],
            educationLevel: [''],
            targetClassId: ['']
        });
    }

    ngOnInit(): void {
        this.classService.getClasses().subscribe(c => this.classes = c);

        this.eventId = Number(this.route.snapshot.paramMap.get('id'));
        if (this.eventId) {
            this.isEditMode = true;
            this.loadEvent(this.eventId);
        }

        // When a specific class is chosen, clear the level restriction and vice-versa
        this.eventForm.get('targetClassId')!.valueChanges.subscribe(val => {
            if (val) this.eventForm.get('educationLevel')!.setValue('', { emitEvent: false });
        });
        this.eventForm.get('educationLevel')!.valueChanges.subscribe(val => {
            if (val) this.eventForm.get('targetClassId')!.setValue('', { emitEvent: false });
        });
    }

    private snapTo15(dateStr: string): string {
        const d = new Date(dateStr);
        d.setMinutes(Math.round(d.getMinutes() / 15) * 15, 0, 0);
        const pad = (n: number) => String(n).padStart(2, '0');
        return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    }

    loadEvent(id: number): void {
        this.eventService.getEventDetails(id).subscribe({
            next: (event) => {
                this.eventForm.patchValue({
                    title: event.title,
                    description: event.description,
                    startTime: this.snapTo15(event.startTime),
                    endTime: this.snapTo15(event.endTime),
                    educationLevel: event.educationLevel ?? '',
                    targetClassId: event.targetClassId ?? ''
                });
            },
            error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to load event'))
        });
    }

    onSubmit(): void {
        if (this.eventForm.valid) {
            this.isSubmitting = true;
            const raw = this.eventForm.value;
            const request = {
                ...raw,
                educationLevel: raw.educationLevel || null,
                targetClassId: raw.targetClassId ? Number(raw.targetClassId) : null
            };

            const obs$ = this.isEditMode
                ? this.eventService.updateEvent(this.eventId!, request)
                : this.eventService.createEvent(request);

            obs$.subscribe({
                next: () => this.router.navigate(['/events']),
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to save event'));
                    this.isSubmitting = false;
                }
            });
        }
    }
}
