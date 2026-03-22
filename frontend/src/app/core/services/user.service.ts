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
