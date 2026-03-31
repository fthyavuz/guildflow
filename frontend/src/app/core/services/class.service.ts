import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ClassResponse } from '../models/class.model';
import { User, UserResponse } from '../models/auth.model';
import { StudentProgressSummary } from '../models/student-progress.model';
import { StudentProfile } from '../models/student.model';
import { PagedResponse } from '../models/page.model';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class ClassService {
    private http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiBaseUrl}/classes`;

    getClasses(): Observable<ClassResponse[]> {
        return this.http.get<PagedResponse<ClassResponse>>(this.apiUrl)
            .pipe(map(res => res.content));
    }

    getAllClassesForEvents(): Observable<ClassResponse[]> {
        return this.http.get<ClassResponse[]>(`${this.apiUrl}/all`);
    }

    getMentorClasses(): Observable<ClassResponse[]> {
        return this.getClasses();
    }

    getClassById(id: number): Observable<ClassResponse> {
        return this.http.get<ClassResponse>(`${this.apiUrl}/${id}`);
    }

    getClassStudents(classId: number): Observable<UserResponse[]> {
        return this.http.get<UserResponse[]>(`${this.apiUrl}/${classId}/students`);
    }

    getClassProgressSummary(classId: number): Observable<StudentProgressSummary[]> {
        return this.http.get<StudentProgressSummary[]>(`${this.apiUrl}/${classId}/progress-summary`);
    }

    getSystemStats(): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/stats`);
    }

    createClass(data: any): Observable<ClassResponse> {
        return this.http.post<ClassResponse>(this.apiUrl, data);
    }

    updateClass(id: number, data: any): Observable<ClassResponse> {
        return this.http.put<ClassResponse>(`${this.apiUrl}/${id}`, data);
    }

    addStudentToClass(classId: number, studentId: number): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/${classId}/students/${studentId}`, {});
    }

    removeStudentFromClass(classId: number, studentId: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${classId}/students/${studentId}`);
    }

    getStudentProfile(studentId: number): Observable<StudentProfile> {
        return this.http.get<StudentProfile>(`${this.apiUrl}/students/${studentId}/profile`);
    }
}
