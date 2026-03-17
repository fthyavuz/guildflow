import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthResponse, User } from '../models/auth.model';
import { Router } from '@angular/router';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private http = inject(HttpClient);
    private router = inject(Router);
    private readonly apiUrl = 'http://localhost:8080/api/auth';

    private currentUserSubject = new BehaviorSubject<User | null>(this.getStoredUser());
    public currentUser$ = this.currentUserSubject.asObservable();

    login(credentials: { email: string; password: string }): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
            tap((response: AuthResponse) => this.handleAuthSuccess(response))
        );
    }

    logout(): void {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('user');
        this.currentUserSubject.next(null);
    }

    private handleAuthSuccess(response: AuthResponse): void {
        localStorage.setItem('access_token', response.accessToken);
        localStorage.setItem('refresh_token', response.refreshToken);
        localStorage.setItem('user', JSON.stringify(response.user));
        this.currentUserSubject.next(response.user);
    }

    private getStoredUser(): User | null {
        const userStr = localStorage.getItem('user');
        const token = localStorage.getItem('access_token');
        
        // If we have a user but no token, or if data is corrupted, clear everything
        if (!userStr || !token) {
            if (userStr || token) this.logout();
            return null;
        }

        try {
            return JSON.parse(userStr);
        } catch (e) {
            this.logout();
            return null;
        }
    }

    getAccessToken(): string | null {
        return localStorage.getItem('access_token');
    }

    getRefreshToken(): string | null {
        return localStorage.getItem('refresh_token');
    }

    refreshToken(): Observable<AuthResponse> {
        const refreshToken = this.getRefreshToken();
        return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
            tap((response: AuthResponse) => this.handleAuthSuccess(response))
        );
    }
}
