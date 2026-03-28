import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { UserResponse } from '../models/auth.model';
import { PagedResponse } from '../models/page.model';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiBaseUrl}/users`;

    getUsersPage(options: { role?: string; search?: string; page?: number; size?: number }): Observable<PagedResponse<UserResponse>> {
        let params = new HttpParams()
            .set('page', String(options.page ?? 0))
            .set('size', String(options.size ?? 20));
        if (options.role) params = params.set('role', options.role);
        if (options.search?.trim()) params = params.set('search', options.search.trim());
        return this.http.get<PagedResponse<UserResponse>>(this.apiUrl, { params });
    }

    getUsers(role?: string): Observable<UserResponse[]> {
        let params = new HttpParams();
        if (role) {
            params = params.set('role', role);
        }
        return this.http.get<PagedResponse<UserResponse>>(this.apiUrl, { params })
            .pipe(map(res => res.content));
    }

    getMentors(): Observable<UserResponse[]> {
        return this.http.get<PagedResponse<UserResponse>>(`${this.apiUrl}/mentors`)
            .pipe(map(res => res.content));
    }

    getStudents(): Observable<UserResponse[]> {
        return this.http.get<PagedResponse<UserResponse>>(`${this.apiUrl}/students`)
            .pipe(map(res => res.content));
    }

    getStudentsList(): Observable<UserResponse[]> {
        return this.http.get<UserResponse[]>(`${this.apiUrl}/students-list`);
    }

    getParentsList(): Observable<UserResponse[]> {
        return this.http.get<UserResponse[]>(`${this.apiUrl}/parents-list`);
    }

    getUserById(id: number): Observable<UserResponse> {
        return this.http.get<UserResponse>(`${this.apiUrl}/${id}`);
    }

    createUser(data: any): Observable<UserResponse> {
        return this.http.post<UserResponse>(this.apiUrl, data);
    }

    updateUser(id: number, data: any): Observable<UserResponse> {
        return this.http.put<UserResponse>(`${this.apiUrl}/${id}`, data);
    }

    deleteUser(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    adminResetPassword(id: number, newPassword: string): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/${id}/admin-reset-password`, { newPassword });
    }

    linkParentToStudent(parentId: number, studentId: number): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/${parentId}/link-student/${studentId}`, {});
    }

    unlinkParentFromStudent(parentId: number, studentId: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${parentId}/link-student/${studentId}`);
    }
}
