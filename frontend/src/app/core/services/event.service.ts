import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
    EventResponse,
    EventDetailsResponse,
    EventRequest,
    EventAssignmentRequest,
    EventAssignmentResponse,
    EventParticipantResponse,
    RsvpRequest
} from '../models/event.model';
import { PagedResponse } from '../models/page.model';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class EventService {
    private http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiBaseUrl}/events`;

    getUpcomingEvents(): Observable<EventResponse[]> {
        return this.http.get<PagedResponse<EventResponse>>(this.apiUrl)
            .pipe(map(res => res.content));
    }

    getEventDetails(id: number): Observable<EventDetailsResponse> {
        return this.http.get<EventDetailsResponse>(`${this.apiUrl}/${id}`);
    }

    createEvent(request: EventRequest): Observable<EventResponse> {
        return this.http.post<EventResponse>(this.apiUrl, request);
    }

    updateEvent(id: number, request: EventRequest): Observable<EventResponse> {
        return this.http.put<EventResponse>(`${this.apiUrl}/${id}`, request);
    }

    deleteEvent(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    rsvpToEvent(id: number, request: RsvpRequest): Observable<EventParticipantResponse> {
        return this.http.post<EventParticipantResponse>(`${this.apiUrl}/${id}/rsvp`, request);
    }

    assignDuty(id: number, request: EventAssignmentRequest): Observable<EventAssignmentResponse> {
        return this.http.post<EventAssignmentResponse>(`${this.apiUrl}/${id}/assignments`, request);
    }

    removeAssignment(assignmentId: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/assignments/${assignmentId}`);
    }
}
