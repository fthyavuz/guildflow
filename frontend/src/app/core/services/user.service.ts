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
}
