import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EventService } from '../../../core/services/event.service';
import { NotificationService } from '../../../core/services/notification.service';
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
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private notifications = inject(NotificationService);

    eventForm: FormGroup;
    isEditMode = false;
    eventId: number | null = null;
    isSubmitting = false;

    constructor() {
        this.eventForm = this.fb.group({
            title: ['', [Validators.required]],
            description: [''],
            startTime: ['', [Validators.required]],
            endTime: ['', [Validators.required]]
        });
    }

    ngOnInit(): void {
        this.eventId = Number(this.route.snapshot.paramMap.get('id'));
        if (this.eventId) {
            this.isEditMode = true;
            this.loadEvent(this.eventId);
        }
    }

    loadEvent(id: number): void {
        this.eventService.getEventDetails(id).subscribe({
            next: (event) => {
                const formatForInput = (dateStr: string) => dateStr.substring(0, 16);

                this.eventForm.patchValue({
                    title: event.title,
                    description: event.description,
                    startTime: formatForInput(event.startTime),
                    endTime: formatForInput(event.endTime)
                });
            },
            error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to load event'))
        });
    }

    onSubmit(): void {
        if (this.eventForm.valid) {
            this.isSubmitting = true;

            const obs$ = this.isEditMode
                ? this.eventService.updateEvent(this.eventId!, this.eventForm.value)
                : this.eventService.createEvent(this.eventForm.value);

            obs$.subscribe({
                next: () => {
                    this.router.navigate(['/events']);
                },
                error: (err) => {
                    this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to save event'));
                    this.isSubmitting = false;
                }
            });
        }
    }
}
