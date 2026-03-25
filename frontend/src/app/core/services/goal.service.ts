import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { GoalProgress, PendingProgressEntry } from '../models/student.model';
import { PagedResponse } from '../models/page.model';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class GoalService {
    private http = inject(HttpClient);
    private readonly goalsApiUrl = `${environment.apiBaseUrl}/goals`;
    private readonly progressApiUrl = `${environment.apiBaseUrl}/progress`;

    getMyGoals(): Observable<GoalProgress[]> {
        return this.http.get<GoalProgress[]>(`${this.goalsApiUrl}/my-goals`);
    }

    submitProgress(taskId: number, entryDate: string, numericValue?: number, booleanValue?: boolean): Observable<void> {
        return this.http.post<void>(this.progressApiUrl, {
            taskId,
            numericValue,
            booleanValue,
            entryDate
        });
    }

    getPendingApprovals(): Observable<PendingProgressEntry[]> {
        return this.http.get<PendingProgressEntry[]>(`${this.progressApiUrl}/pending`);
    }

    approveEntry(entryId: number, mentorNotes?: string): Observable<void> {
        return this.http.post<void>(`${this.progressApiUrl}/${entryId}/approve`, { mentorNotes });
    }

    rejectEntry(entryId: number, mentorNotes?: string): Observable<void> {
        return this.http.post<void>(`${this.progressApiUrl}/${entryId}/reject`, { mentorNotes });
    }

    getGoalTypes(): Observable<any[]> {
        return this.http.get<any[]>(`${this.goalsApiUrl}/types`);
    }

    createGoal(goal: any): Observable<any> {
        return this.http.post<any>(this.goalsApiUrl, goal);
    }

    updateGoal(id: number, goal: any): Observable<any> {
        return this.http.put<any>(`${this.goalsApiUrl}/${id}`, goal);
    }

    getGoalsForClass(classId: number): Observable<any[]> {
        return this.http.get<PagedResponse<any>>(`${this.goalsApiUrl}/class/${classId}`)
            .pipe(map(res => res.content));
    }

    getTemplates(): Observable<any[]> {
        return this.http.get<PagedResponse<any>>(`${this.goalsApiUrl}/templates`)
            .pipe(map(res => res.content));
    }

    assignTemplate(assignment: any): Observable<any> {
        return this.http.post<any>(`${this.goalsApiUrl}/assign-template`, assignment);
    }

    getGoalById(id: number): Observable<any> {
        return this.http.get<any>(`${this.goalsApiUrl}/${id}`);
    }

    deleteGoal(id: number): Observable<void> {
        return this.http.delete<void>(`${this.goalsApiUrl}/${id}`);
    }
}
