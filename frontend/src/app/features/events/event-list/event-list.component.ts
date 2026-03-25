import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EventService } from '../../../core/services/event.service';
import { AuthService } from '../../../core/services/auth.service';
import { ClassService } from '../../../core/services/class.service';
import { EventResponse, EventFilterParams } from '../../../core/models/event.model';
import { ClassResponse } from '../../../core/models/class.model';
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
    private classService = inject(ClassService);

    user$ = this.authService.currentUser$;
    classes: ClassResponse[] = [];

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

    ngOnInit(): void {
        this.classService.getClasses().subscribe(c => this.classes = c);
    }

    setTimeFilter(filter: EventFilterParams['filter']): void {
        this.filters$.next({ ...this.filters$.value, filter });
    }

    setLevelFilter(educationLevel: string): void {
        this.filters$.next({ ...this.filters$.value, educationLevel: educationLevel || undefined, classId: undefined });
    }

    setClassFilter(classId: string): void {
        this.filters$.next({ ...this.filters$.value, classId: classId || undefined, educationLevel: undefined });
    }

    levelLabel(level: string | null): string {
        if (!level) return '';
        return this.educationLevels.find(l => l.value === level)?.label ?? level;
    }
}
