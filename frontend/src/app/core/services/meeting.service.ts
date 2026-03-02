import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MeetingResponse, MeetingRequest } from '../models/meeting.model';

@Injectable({
    providedIn: 'root'
})
export class MeetingService {
    private http = inject(HttpClient);
    private readonly apiUrl = 'http://localhost:8080/api/meetings';

    getMyMeetings(): Observable<MeetingResponse[]> {
        return this.http.get<MeetingResponse[]>(`${this.apiUrl}/my`);
    }

    getMeetingsForClass(classId: number): Observable<MeetingResponse[]> {
        return this.http.get<MeetingResponse[]>(`${this.apiUrl}/class/${classId}`);
    }

    createMeeting(request: MeetingRequest): Observable<MeetingResponse[]> {
        return this.http.post<MeetingResponse[]>(this.apiUrl, request);
    }
}
