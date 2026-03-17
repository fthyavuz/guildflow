import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserResponse } from '../models/auth.model';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private http = inject(HttpClient);
    private readonly apiUrl = 'http://localhost:8080/api/users';

    getUsers(role?: string): Observable<UserResponse[]> {
        let params = new HttpParams();
        if (role) {
            params = params.set('role', role);
        }
        return this.http.get<UserResponse[]>(this.apiUrl, { params });
    }

    getMentors(): Observable<UserResponse[]> {
        return this.http.get<UserResponse[]>(`${this.apiUrl}/mentors`);
    }

    getStudents(): Observable<UserResponse[]> {
        return this.http.get<UserResponse[]>(`${this.apiUrl}/students`);
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
}
