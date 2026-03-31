import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { EventService } from '../../../core/services/event.service';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { EventDetailsResponse, EventAssignmentResponse } from '../../../core/models/event.model';
import { UserResponse } from '../../../core/models/auth.model';
import { Observable, forkJoin, map } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { FilterGoingPipe, FilterNotGoingPipe } from '../../../core/pipes/event.pipe';

@Component({
    selector: 'app-event-detail',
    standalone: true,
    imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule, TranslateModule, FilterGoingPipe, FilterNotGoingPipe],
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

    // User search state (for assign duty form)
    private allUsersCache: UserResponse[] = [];
    filteredUsers: UserResponse[] = [];
    userSearchText = '';
    selectedUserName = '';
    showUserDropdown = false;

    // Eligible students for manual add
    eligibleStudents: UserResponse[] = [];
    filteredEligible: UserResponse[] = [];
    addParticipantSearchText = '';
    selectedAddUserId: number | null = null;
    selectedAddUserName = '';
    showEligibleDropdown = false;
    showAddParticipantPanel = false;
    isAddingParticipant = false;

    // Inline assignment edit state
    editingAssignmentId: number | null = null;
    editDutyDescription = '';
    editUserId: number | null = null;
    editUserSearchText = '';
    editSelectedUserName = '';
    editFilteredUsers: UserResponse[] = [];
    showEditUserDropdown = false;
    isSavingEdit = false;

    ngOnInit(): void {
        this.eventId = Number(this.route.snapshot.paramMap.get('id'));
        this.loadEventDetails();

        this.user$.subscribe(user => {
            if (user?.role === 'ADMIN' || user?.role === 'MENTOR') {
                forkJoin({
                    mentors: this.userService.getMentors(),
                    students: this.userService.getStudents()
                }).pipe(
                    map(({ mentors, students }) => [...mentors, ...students])
                ).subscribe(users => {
                    this.allUsersCache = users;
                    this.filteredUsers = users;
                    this.editFilteredUsers = users;
                });
                this.loadEligibleStudents();
            }
        });
    }

    onUserSearch(event: Event): void {
        const term = (event.target as HTMLInputElement).value.toLowerCase();
        this.userSearchText = (event.target as HTMLInputElement).value;
        this.showUserDropdown = true;
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
        setTimeout(() => {
            this.showUserDropdown = false;
            if (!this.selectedUserName) {
                this.userSearchText = '';
                this.assignmentForm.patchValue({ userId: '' });
            }
        }, 200);
    }

    loadEventDetails(): void {
        this.event$ = this.eventService.getEventDetails(this.eventId);
    }

    loadEligibleStudents(): void {
        this.eventService.getEligibleStudents(this.eventId).subscribe({
            next: (students) => {
                this.eligibleStudents = students;
                this.filteredEligible = students;
            },
            error: () => {}
        });
    }

    // --- Eligible student search for add participant ---
    onEligibleSearch(event: Event): void {
        const term = (event.target as HTMLInputElement).value.toLowerCase();
        this.addParticipantSearchText = (event.target as HTMLInputElement).value;
        this.showEligibleDropdown = true;
        this.selectedAddUserId = null;
        this.selectedAddUserName = '';
        this.filteredEligible = this.eligibleStudents.filter(u =>
            `${u.firstName} ${u.lastName}`.toLowerCase().includes(term)
        );
    }

    selectEligible(user: UserResponse): void {
        this.selectedAddUserId = user.id;
        this.selectedAddUserName = `${user.firstName} ${user.lastName}`;
        this.addParticipantSearchText = this.selectedAddUserName;
        this.showEligibleDropdown = false;
    }

    onEligibleBlur(): void {
        setTimeout(() => {
            this.showEligibleDropdown = false;
            if (!this.selectedAddUserId) {
                this.addParticipantSearchText = '';
            }
        }, 200);
    }

    addParticipant(): void {
        if (!this.selectedAddUserId) return;
        this.isAddingParticipant = true;
        this.eventService.addParticipantManually(this.eventId, this.selectedAddUserId).subscribe({
            next: () => {
                this.isAddingParticipant = false;
                this.showAddParticipantPanel = false;
                this.selectedAddUserId = null;
                this.selectedAddUserName = '';
                this.addParticipantSearchText = '';
                this.loadEventDetails();
                this.loadEligibleStudents();
            },
            error: (err) => {
                this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to add participant'));
                this.isAddingParticipant = false;
            }
        });
    }

    removeParticipant(participantId: number): void {
        if (confirm('Remove this participant from the event?')) {
            this.eventService.removeParticipant(this.eventId, participantId).subscribe({
                next: () => {
                    this.loadEventDetails();
                    this.loadEligibleStudents();
                },
                error: (err) => this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to remove participant'))
            });
        }
    }

    // --- Inline assignment edit ---
    startEditAssignment(assignment: EventAssignmentResponse): void {
        this.editingAssignmentId = assignment.id;
        this.editDutyDescription = assignment.dutyDescription;
        this.editUserId = assignment.userId;
        this.editSelectedUserName = assignment.userName;
        this.editUserSearchText = assignment.userName;
        this.editFilteredUsers = this.allUsersCache;
        this.showEditUserDropdown = false;
    }

    onEditUserSearch(event: Event): void {
        const term = (event.target as HTMLInputElement).value.toLowerCase();
        this.editUserSearchText = (event.target as HTMLInputElement).value;
        this.showEditUserDropdown = true;
        this.editUserId = null;
        this.editSelectedUserName = '';
        this.editFilteredUsers = this.allUsersCache.filter(u =>
            `${u.firstName} ${u.lastName}`.toLowerCase().includes(term) ||
            u.role.toLowerCase().includes(term)
        );
    }

    selectEditUser(user: UserResponse): void {
        this.editUserId = user.id;
        this.editSelectedUserName = `${user.firstName} ${user.lastName}`;
        this.editUserSearchText = this.editSelectedUserName;
        this.showEditUserDropdown = false;
    }

    onEditUserBlur(): void {
        setTimeout(() => {
            this.showEditUserDropdown = false;
            if (!this.editUserId) {
                this.editUserSearchText = this.editSelectedUserName;
            }
        }, 200);
    }

    saveEditAssignment(): void {
        if (!this.editingAssignmentId || !this.editUserId || !this.editDutyDescription.trim()) return;
        this.isSavingEdit = true;
        this.eventService.updateAssignment(this.editingAssignmentId, {
            userId: this.editUserId,
            dutyDescription: this.editDutyDescription
        }).subscribe({
            next: () => {
                this.isSavingEdit = false;
                this.editingAssignmentId = null;
                this.loadEventDetails();
            },
            error: (err) => {
                this.notifications.error(this.notifications.extractErrorMessage(err, 'Failed to update assignment'));
                this.isSavingEdit = false;
            }
        });
    }

    cancelEditAssignment(): void {
        this.editingAssignmentId = null;
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
                this.userSearchText = '';
                this.selectedUserName = '';
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

    canManageEvent(event: any, user: any): boolean {
        if (!user || !event) return false;
        if (this.isPast(event)) return false;
        return user.role === 'ADMIN' || (user.role === 'MENTOR' && event.createdById === user.id);
    }

    isPast(event: any): boolean {
        return new Date(event.endTime) < new Date();
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
