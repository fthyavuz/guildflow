import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { MeetingResponse, MeetingRequest } from '../models/meeting.model';
import { PagedResponse } from '../models/page.model';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class MeetingService {
    private http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiBaseUrl}/meetings`;

    getMyMeetings(): Observable<MeetingResponse[]> {
        return this.http.get<PagedResponse<MeetingResponse>>(`${this.apiUrl}/my`)
            .pipe(map(res => res.content));
    }

    getMeetingsForClass(classId: number): Observable<MeetingResponse[]> {
        return this.http.get<PagedResponse<MeetingResponse>>(`${this.apiUrl}/class/${classId}`)
            .pipe(map(res => res.content));
    }

    createMeeting(request: MeetingRequest): Observable<MeetingResponse[]> {
        return this.http.post<MeetingResponse[]>(this.apiUrl, request);
    }
}
