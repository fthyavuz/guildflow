import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Observable, catchError, switchMap, throwError } from 'rxjs';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (
    req: HttpRequest<unknown>,
    next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const token = authService.getAccessToken();

    let authReq = req;
    if (token && !req.url.includes('/auth/login') && !req.url.includes('/auth/refresh')) {
        authReq = req.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
    }

    return next(authReq).pipe(
        catchError((error: HttpErrorResponse) => {
            // If 401 on a non-refresh request, attempt a token refresh (with mutex)
            if (error.status === 401 && !req.url.includes('/auth/refresh')) {
                return authService.handleTokenRefresh().pipe(
                    switchMap((newToken: string) => {
                        const newReq = req.clone({
                            setHeaders: { Authorization: `Bearer ${newToken}` }
                        });
                        return next(newReq);
                    }),
                    catchError((err: any) => {
                        // Only force logout if the refresh endpoint explicitly returned 401.
                        // Network timeouts (status 0) or server errors (5xx) should not
                        // log the user out — the session may still be valid.
                        if (err instanceof HttpErrorResponse && err.status === 401) {
                            authService.logout();
                            router.navigate(['/login']);
                        }
                        return throwError(() => err);
                    })
                );
            }

            // 401 on the refresh endpoint itself — session expired, force logout
            if (error.status === 401) {
                authService.logout();
                router.navigate(['/login']);
            }

            return throwError(() => error);
        })
    );
};
