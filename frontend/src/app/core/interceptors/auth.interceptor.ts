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
            // If 401 and not login/refresh, try to refresh token
            if (error.status === 401 && !req.url.includes('/auth/refresh')) {
                return authService.refreshToken().pipe(
                    switchMap((response: any) => {
                        const newReq = req.clone({
                            setHeaders: {
                                Authorization: `Bearer ${response.accessToken}`
                            }
                        });
                        return next(newReq);
                    }),
                    catchError((err: any) => {
                        authService.logout();
                        router.navigate(['/login']);
                        return throwError(() => err);
                    })
                );
            }
            
            // If any other 401, just logout and redirect
            if (error.status === 401) {
                authService.logout();
                router.navigate(['/login']);
            }
            
            return throwError(() => error);
        })
    );
};
