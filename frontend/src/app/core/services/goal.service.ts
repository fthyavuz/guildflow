import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GoalProgress } from '../models/student.model';

@Injectable({
    providedIn: 'root'
})
export class GoalService {
    private http = inject(HttpClient);
    private readonly goalsApiUrl = 'http://localhost:8080/api/goals';
    private readonly progressApiUrl = 'http://localhost:8080/api/progress';

    getMyGoals(): Observable<GoalProgress[]> {
        return this.http.get<GoalProgress[]>(`${this.goalsApiUrl}/my-goals`);
    }

    submitProgress(taskId: number, numericValue?: number, booleanValue?: boolean): Observable<void> {
        return this.http.post<void>(this.progressApiUrl, {
            taskId,
            numericValue,
            booleanValue,
            entryDate: new Date().toISOString().split('T')[0]
        });
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
        return this.http.get<any[]>(`${this.goalsApiUrl}/class/${classId}`);
    }

    getTemplates(): Observable<any[]> {
        return this.http.get<any[]>(`${this.goalsApiUrl}/templates`);
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
