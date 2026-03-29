import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { MeetingResponse, MeetingRequest, MeetingUpdateRequest, AttendanceRecord } from '../models/meeting.model';
import { AttendanceSummary } from '../models/student.model';
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

    getMeetingById(id: number): Observable<MeetingResponse> {
        return this.http.get<MeetingResponse>(`${this.apiUrl}/${id}`);
    }

    getMeetingsForClass(classId: number): Observable<MeetingResponse[]> {
        return this.http.get<PagedResponse<MeetingResponse>>(`${this.apiUrl}/class/${classId}`)
            .pipe(map(res => res.content));
    }

    createMeeting(request: MeetingRequest): Observable<MeetingResponse[]> {
        return this.http.post<MeetingResponse[]>(this.apiUrl, request);
    }

    updateMeeting(id: number, request: MeetingUpdateRequest): Observable<MeetingResponse> {
        return this.http.put<MeetingResponse>(`${this.apiUrl}/${id}`, request);
    }

    deleteMeeting(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    getAttendance(meetingId: number): Observable<AttendanceRecord[]> {
        return this.http.get<AttendanceRecord[]>(`${this.apiUrl}/${meetingId}/attendance`);
    }

    markAttendance(meetingId: number, records: { studentId: number; status: string; note: string }[]): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/${meetingId}/attendance`, records);
    }

    getStudentAttendanceSummary(studentId: number): Observable<AttendanceSummary> {
        return this.http.get<AttendanceSummary>(`${this.apiUrl}/student/${studentId}/attendance-summary`);
    }
}
