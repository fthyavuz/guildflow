import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EventService } from '../../../core/services/event.service';
import { ClassService } from '../../../core/services/class.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ClassResponse } from '../../../core/models/class.model';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-event-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterModule, TranslateModule],
    templateUrl: './event-form.component.html',
    styleUrl: './event-form.component.css'
})
export class EventFormComponent implements OnInit {
    private fb = inject(FormBuilder);
    private eventService = inject(EventService);
    private classService = inject(ClassService);
    private authService = inject(AuthService);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private notifications = inject(NotificationService);

    eventForm: FormGroup;
    isEditMode = false;
    eventId: number | null = null;
    isSubmitting = false;

    // Class picker
    allClasses: ClassResponse[] = [];
    levelFilter = '';
    nameFilter = '';
    selectedClassIds: Set<number> = new Set();

    readonly educationLevels = [
        { value: '', label: 'All Levels' },
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
            endTime: ['', [Validators.required]]
        });
    }

    ngOnInit(): void {
        this.classService.getAllClassesForEvents().subscribe(classes => {
            this.allClasses = classes;

            // Auto-select mentor's own class on new event creation
            if (!this.isEditMode) {
                this.authService.currentUser$.subscribe(user => {
                    if (user?.role === 'MENTOR') {
                        classes.filter(c => c.mentorId === user.id)
                            .forEach(c => this.selectedClassIds.add(c.id));
                    }
                });
            }
        });

        this.eventId = Number(this.route.snapshot.paramMap.get('id')) || null;
        if (this.eventId) {
            this.isEditMode = true;
            this.loadEvent(this.eventId);
        }
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
                    endTime: this.snapTo15(event.endTime)
                });
                this.selectedClassIds = new Set(event.targetClassIds);
            },
            error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to load event'))
        });
    }

    get filteredClasses(): ClassResponse[] {
        return this.allClasses.filter(c => {
            const levelMatch = !this.levelFilter || c.educationLevel === this.levelFilter;
            const nameMatch = !this.nameFilter ||
                c.name.toLowerCase().includes(this.nameFilter.toLowerCase());
            return levelMatch && nameMatch;
        });
    }

    isSelected(classId: number): boolean {
        return this.selectedClassIds.has(classId);
    }

    toggleClass(classId: number): void {
        if (this.selectedClassIds.has(classId)) {
            this.selectedClassIds.delete(classId);
        } else {
            this.selectedClassIds.add(classId);
        }
    }

    selectAllFiltered(): void {
        this.filteredClasses.forEach(c => this.selectedClassIds.add(c.id));
    }

    deselectAllFiltered(): void {
        this.filteredClasses.forEach(c => this.selectedClassIds.delete(c.id));
    }

    allFilteredSelected(): boolean {
        return this.filteredClasses.length > 0 &&
            this.filteredClasses.every(c => this.selectedClassIds.has(c.id));
    }

    get levelGroups(): string[] {
        const levels = new Set(this.allClasses.map(c => c.educationLevel as string));
        return Array.from(levels).sort();
    }

    selectByLevel(level: string): void {
        this.allClasses.filter(c => c.educationLevel === level)
            .forEach(c => this.selectedClassIds.add(c.id));
    }

    onSubmit(): void {
        if (this.eventForm.valid) {
            this.isSubmitting = true;
            const raw = this.eventForm.value;
            const request = {
                ...raw,
                targetClassIds: [...this.selectedClassIds]
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
