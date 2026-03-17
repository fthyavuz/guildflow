import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { EventService } from '../../../core/services/event.service';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { EventDetailsResponse } from '../../../core/models/event.model';
import { UserResponse } from '../../../core/models/auth.model';
import { Observable, switchMap, forkJoin, map, of } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { FilterGoingPipe, FilterNotGoingPipe } from '../../../core/pipes/event.pipe';

@Component({
    selector: 'app-event-detail',
    standalone: true,
    imports: [CommonModule, RouterModule, ReactiveFormsModule, TranslateModule, FilterGoingPipe, FilterNotGoingPipe],
    templateUrl: './event-detail.component.html',
    styleUrl: './event-detail.component.css'
})
export class EventDetailComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private eventService = inject(EventService);
    private userService = inject(UserService);
    private authService = inject(AuthService);
    private fb = inject(FormBuilder);

    event$: Observable<EventDetailsResponse> | undefined;
    allUsers$: Observable<UserResponse[]> | undefined;
    user$ = this.authService.currentUser$;
    
    eventId: number = 0;
    assignmentForm = this.fb.group({
        userId: ['', [Validators.required]],
        dutyDescription: ['', [Validators.required]]
    });

    isSubmittingRsvp = false;
    isSubmittingAssignment = false;

    ngOnInit(): void {
        this.eventId = Number(this.route.snapshot.paramMap.get('id'));
        this.loadEventDetails();
        
        // Load all mentors and students for the assignment dropdown (Admin only)
        this.user$.subscribe(user => {
            if (user?.role === 'ADMIN') {
                this.allUsers$ = forkJoin({
                    mentors: this.userService.getMentors(),
                    students: this.userService.getStudents()
                }).pipe(
                    map(({mentors, students}) => [...mentors, ...students])
                );
            }
        });
    }

    loadEventDetails(): void {
        this.event$ = this.eventService.getEventDetails(this.eventId);
    }

    rsvp(isGoing: boolean): void {
        this.isSubmittingRsvp = true;
        this.eventService.rsvpToEvent(this.eventId, { isGoing }).subscribe({
            next: () => {
                this.loadEventDetails();
                this.isSubmittingRsvp = false;
            },
            error: (err) => {
                console.error('RSVP error:', err);
                this.isSubmittingRsvp = false;
            }
        });
    }

    assignDuty(): void {
        if (this.assignmentForm.invalid) return;

        this.isSubmittingAssignment = true;
        const formValue = this.assignmentForm.value;
        const request = {
            userId: Number(formValue.userId),
            dutyDescription: formValue.dutyDescription as string
        };

        this.eventService.assignDuty(this.eventId, request).subscribe({
            next: () => {
                this.loadEventDetails();
                this.assignmentForm.reset();
                this.isSubmittingAssignment = false;
            },
            error: (err) => {
                console.error('Assignment error:', err);
                this.isSubmittingAssignment = false;
            }
        });
    }

    removeAssignment(id: number): void {
        if (confirm('Are you sure you want to remove this assignment?')) {
            this.eventService.removeAssignment(id).subscribe({
                next: () => this.loadEventDetails(),
                error: (err) => console.error('Remove assignment error:', err)
            });
        }
    }

    deleteEvent(): void {
        if (confirm('Are you sure you want to delete this event? This action cannot be undone.')) {
            this.eventService.deleteEvent(this.eventId).subscribe({
                next: () => this.router.navigate(['/events']),
                error: (err) => console.error('Delete event error:', err)
            });
        }
    }
}
