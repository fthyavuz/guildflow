import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EventService } from '../../../core/services/event.service';
import { ClassService } from '../../../core/services/class.service';
import { RoomService } from '../../../core/services/room.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ClassResponse } from '../../../core/models/class.model';
import { Room, RoomBooking } from '../../../core/models/room.model';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';

@Component({
    selector: 'app-event-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterModule, TranslateModule],
    templateUrl: './event-form.component.html',
    styleUrl: './event-form.component.css'
})
export class EventFormComponent implements OnInit, OnDestroy {
    private fb = inject(FormBuilder);
    private eventService = inject(EventService);
    private classService = inject(ClassService);
    private roomService = inject(RoomService);
    private authService = inject(AuthService);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private notifications = inject(NotificationService);
    private destroy$ = new Subject<void>();

    eventForm: FormGroup;
    isEditMode = false;
    eventId: number | null = null;
    isSubmitting = false;
    isPastEvent = false;

    // Date constraints
    readonly todayMin: string = (() => {
        const d = new Date();
        const pad = (n: number) => String(n).padStart(2, '0');
        return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T00:00`;
    })();

    // Class picker
    allClasses: ClassResponse[] = [];
    levelFilter = '';
    nameFilter = '';
    selectedClassIds: Set<number> = new Set();

    // Room picker
    rooms: Room[] = [];
    selectedRoomId: number | null = null;
    roomConflict = false;
    conflictingBooking: RoomBooking | null = null;
    checkingAvailability = false;

    readonly educationLevels = [
        { value: '', label: 'All Levels' },
        { value: 'PRIMARY', label: 'Primary' },
        { value: 'SECONDARY', label: 'Secondary' },
        { value: 'HIGH_SCHOOL', label: 'High School' },
        { value: 'UNIVERSITY', label: 'University' }
    ];

    constructor() {
        const pad = (n: number) => String(n).padStart(2, '0');
        const today = new Date();
        const dateStr = `${today.getFullYear()}-${pad(today.getMonth() + 1)}-${pad(today.getDate())}`;

        this.eventForm = this.fb.group({
            title: ['', [Validators.required]],
            description: [''],
            startTime: [`${dateStr}T09:00`, [Validators.required]],
            endTime: [`${dateStr}T17:00`, [Validators.required]]
        });
    }

    ngOnInit(): void {
        this.classService.getAllClassesForEvents().subscribe(classes => {
            this.allClasses = classes;
            if (!this.isEditMode) {
                this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe(user => {
                    if (user?.role === 'MENTOR') {
                        classes.filter(c => c.mentorId === user.id)
                            .forEach(c => this.selectedClassIds.add(c.id));
                    }
                });
            }
        });

        this.roomService.getAllRooms().subscribe(rooms => this.rooms = rooms);

        this.eventId = Number(this.route.snapshot.paramMap.get('id')) || null;
        if (this.eventId) {
            this.isEditMode = true;
            this.loadEvent(this.eventId);
        }
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
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
                this.isPastEvent = new Date(event.endTime) < new Date();

                this.eventForm.patchValue({
                    title: event.title,
                    description: event.description,
                    startTime: this.snapTo15(event.startTime),
                    endTime: this.snapTo15(event.endTime)
                });
                this.selectedClassIds = new Set(event.targetClassIds);
                this.selectedRoomId = event.roomId ?? null;

                if (this.isPastEvent) {
                    this.eventForm.disable();
                }
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

    // Room methods
    get selectedRoom(): Room | null {
        return this.rooms.find(r => r.id === this.selectedRoomId) ?? null;
    }

    onRoomChange(): void {
        this.roomConflict = false;
        this.conflictingBooking = null;
        if (this.selectedRoomId) {
            this.checkRoomAvailability();
        }
    }

    checkRoomAvailability(): void {
        const { startTime, endTime } = this.eventForm.value;
        if (!this.selectedRoomId || !startTime || !endTime) return;

        const start = new Date(startTime);
        const end = new Date(endTime);
        this.checkingAvailability = true;

        this.roomService.getBookingsForDate(start).subscribe({
            next: (bookings) => {
                const conflicts = bookings.filter(b => {
                    if (b.roomId !== this.selectedRoomId) return false;
                    const bStart = new Date(b.startTime);
                    const bEnd = new Date(b.endTime);
                    return bStart < end && bEnd > start;
                });
                this.roomConflict = conflicts.length > 0;
                this.conflictingBooking = conflicts[0] ?? null;
                this.checkingAvailability = false;
            },
            error: () => { this.checkingAvailability = false; }
        });
    }

    private toLocalISO(dateStr: string): string {
        const d = new Date(dateStr);
        const pad = (n: number) => String(n).padStart(2, '0');
        return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;
    }

    onSubmit(): void {
        if (this.eventForm.valid) {
            this.isSubmitting = true;
            const raw = this.eventForm.value;
            const request = {
                ...raw,
                startTime: this.toLocalISO(raw.startTime),
                endTime: this.toLocalISO(raw.endTime),
                targetClassIds: [...this.selectedClassIds],
                roomId: this.selectedRoomId ?? null
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
