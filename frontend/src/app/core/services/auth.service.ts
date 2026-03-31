import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, map, shareReplay, tap, throwError } from 'rxjs';
import { AuthResponse, User } from '../models/auth.model';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private http = inject(HttpClient);
    private router = inject(Router);
    private readonly apiUrl = `${environment.apiBaseUrl}/auth`;

    private currentUserSubject = new BehaviorSubject<User | null>(this.getStoredUser());
    public currentUser$ = this.currentUserSubject.asObservable();

    // Mutex: shared in-flight refresh observable. All concurrent 401s subscribe to
    // the same observable so only one HTTP call is made. Cleared on completion/error.
    private refreshInProgress$: Observable<string> | null = null;

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

    getCurrentUser(): User | null {
        return this.currentUserSubject.value;
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

    changePassword(data: { currentPassword: string; newPassword: string }): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/password`, data);
    }

    /**
     * Returns an Observable of the new access token, ensuring only one refresh
     * HTTP call is in flight at a time. Concurrent callers share the same observable
     * via shareReplay(1) and all receive the new token (or the same error) together.
     */
    handleTokenRefresh(): Observable<string> {
        if (!this.refreshInProgress$) {
            this.refreshInProgress$ = this.refreshToken().pipe(
                map(response => response.accessToken),
                tap(() => { this.refreshInProgress$ = null; }),
                catchError(err => {
                    this.refreshInProgress$ = null;
                    return throwError(() => err);
                }),
                shareReplay(1)
            );
        }
        return this.refreshInProgress$;
    }
}
