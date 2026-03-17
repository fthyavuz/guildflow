import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { EventService } from '../../../core/services/event.service';
import { AuthService } from '../../../core/services/auth.service';
import { EventResponse } from '../../../core/models/event.model';
import { Observable } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-event-list',
    standalone: true,
    imports: [CommonModule, RouterModule, TranslateModule],
    templateUrl: './event-list.component.html',
    styleUrl: './event-list.component.css'
})
export class EventListComponent implements OnInit {
    private eventService = inject(EventService);
    private authService = inject(AuthService);

    events$: Observable<EventResponse[]> | undefined;
    user$ = this.authService.currentUser$;

    ngOnInit(): void {
        this.events$ = this.eventService.getUpcomingEvents();
    }
}
