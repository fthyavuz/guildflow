import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Notification {
    id: string;
    type: 'success' | 'error' | 'info';
    message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
    private _notifications$ = new BehaviorSubject<Notification[]>([]);
    notifications$ = this._notifications$.asObservable();

    success(message: string, durationMs = 4000): void {
        this.add({ type: 'success', message }, durationMs);
    }

    error(message: string, durationMs = 6000): void {
        this.add({ type: 'error', message }, durationMs);
    }

    info(message: string, durationMs = 4000): void {
        this.add({ type: 'info', message }, durationMs);
    }

    dismiss(id: string): void {
        this._notifications$.next(
            this._notifications$.getValue().filter(n => n.id !== id)
        );
    }

    /** Extract a human-readable message from an HTTP error response. */
    extractErrorMessage(err: any, fallback = 'An unexpected error occurred'): string {
        return err?.error?.message || err?.message || fallback;
    }

    private add(notification: Omit<Notification, 'id'>, durationMs: number): void {
        const id = Math.random().toString(36).slice(2);
        const current = this._notifications$.getValue();
        this._notifications$.next([...current, { ...notification, id }]);

        setTimeout(() => this.dismiss(id), durationMs);
    }
}
