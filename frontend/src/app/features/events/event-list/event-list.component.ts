import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EventService } from '../../../core/services/event.service';
import { AuthService } from '../../../core/services/auth.service';
import { EventResponse, EventFilterParams } from '../../../core/models/event.model';
import { BehaviorSubject, Observable, switchMap } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-event-list',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
    templateUrl: './event-list.component.html',
    styleUrl: './event-list.component.css'
})
export class EventListComponent implements OnInit {
    private eventService = inject(EventService);
    private authService = inject(AuthService);

    user$ = this.authService.currentUser$;

    readonly timeFilters: { value: EventFilterParams['filter']; label: string }[] = [
        { value: 'UPCOMING', label: 'Upcoming' },
        { value: 'PAST', label: 'Past' },
        { value: 'ALL', label: 'All' }
    ];

    readonly educationLevels = [
        { value: '', label: 'All Levels' },
        { value: 'PRIMARY', label: 'Primary' },
        { value: 'SECONDARY', label: 'Secondary' },
        { value: 'HIGH_SCHOOL', label: 'High School' },
        { value: 'UNIVERSITY', label: 'University' }
    ];

    private filters$ = new BehaviorSubject<EventFilterParams>({ filter: 'UPCOMING' });
    events$: Observable<EventResponse[]> = this.filters$.pipe(
        switchMap(params => this.eventService.getEvents(params))
    );

    get activeFilter() { return this.filters$.value; }

    ngOnInit(): void {}

    setTimeFilter(filter: EventFilterParams['filter']): void {
        this.filters$.next({ ...this.filters$.value, filter });
    }

    setLevelFilter(educationLevel: string): void {
        this.filters$.next({ ...this.filters$.value, educationLevel: educationLevel || undefined });
    }

    canManageEvent(event: any, user: any): boolean {
        if (!user) return false;
        return user.role === 'ADMIN' || (user.role === 'MENTOR' && event.createdById === user.id);
    }
}
