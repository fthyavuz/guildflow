import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { EventService } from '../../../core/services/event.service';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { EventDetailsResponse } from '../../../core/models/event.model';
import { UserResponse } from '../../../core/models/auth.model';
import { Observable, forkJoin, map } from 'rxjs';
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
    private notifications = inject(NotificationService);
    private fb = inject(FormBuilder);

    event$: Observable<EventDetailsResponse> | undefined;
    user$ = this.authService.currentUser$;

    eventId: number = 0;
    assignmentForm = this.fb.group({
        userId: ['', [Validators.required]],
        dutyDescription: ['', [Validators.required]]
    });

    isSubmittingRsvp = false;
    isSubmittingAssignment = false;

    // User search state
    private allUsersCache: UserResponse[] = [];
    filteredUsers: UserResponse[] = [];
    userSearchText = '';
    selectedUserName = '';
    showUserDropdown = false;

    ngOnInit(): void {
        this.eventId = Number(this.route.snapshot.paramMap.get('id'));
        this.loadEventDetails();

        this.user$.subscribe(user => {
            if (user?.role === 'ADMIN') {
                forkJoin({
                    mentors: this.userService.getMentors(),
                    students: this.userService.getStudents()
                }).pipe(
                    map(({ mentors, students }) => [...mentors, ...students])
                ).subscribe(users => {
                    this.allUsersCache = users;
                    this.filteredUsers = users;
                });
            }
        });
    }

    onUserSearch(event: Event): void {
        const term = (event.target as HTMLInputElement).value.toLowerCase();
        this.userSearchText = (event.target as HTMLInputElement).value;
        this.showUserDropdown = true;
        // Clear selection if user is typing something new
        this.assignmentForm.patchValue({ userId: '' });
        this.selectedUserName = '';
        this.filteredUsers = this.allUsersCache.filter(u =>
            `${u.firstName} ${u.lastName}`.toLowerCase().includes(term) ||
            u.role.toLowerCase().includes(term)
        );
    }

    selectUser(user: UserResponse): void {
        this.assignmentForm.patchValue({ userId: String(user.id) });
        this.selectedUserName = `${user.firstName} ${user.lastName}`;
        this.userSearchText = this.selectedUserName;
        this.showUserDropdown = false;
    }

    onUserSearchBlur(): void {
        // Delay so click on dropdown item fires before the list disappears
        setTimeout(() => {
            this.showUserDropdown = false;
            // If no valid selection was made, clear the input
            if (!this.selectedUserName) {
                this.userSearchText = '';
                this.assignmentForm.patchValue({ userId: '' });
            }
        }, 200);
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
                this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to submit RSVP'));
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
                this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to assign duty'));
                this.isSubmittingAssignment = false;
            }
        });
    }

    removeAssignment(id: number): void {
        if (confirm('Are you sure you want to remove this assignment?')) {
            this.eventService.removeAssignment(id).subscribe({
                next: () => this.loadEventDetails(),
                error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to remove assignment'))
            });
        }
    }

    deleteEvent(): void {
        if (confirm('Are you sure you want to delete this event? This action cannot be undone.')) {
            this.eventService.deleteEvent(this.eventId).subscribe({
                next: () => this.router.navigate(['/events']),
                error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to delete event'))
            });
        }
    }
}
