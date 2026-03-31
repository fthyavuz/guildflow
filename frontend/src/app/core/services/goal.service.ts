import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { HomeworkSummary, DayEntry, StudentReport, StudentSummary, DailyProgressEntry, GoalProgress, TaskProgress } from '../models/student.model';
import { PagedResponse } from '../models/page.model';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class GoalService {
    private http = inject(HttpClient);
    private readonly goalsApiUrl = `${environment.apiBaseUrl}/goals`;
    private readonly reportApiUrl = `${environment.apiBaseUrl}/report`;

    // ── Student homework ────────────────────────────────────────────────────

    getMyGoals(): Observable<GoalProgress[]> {
        return this.http.get<GoalProgress[]>(`${this.goalsApiUrl}/my-goals`);
    }

    submitProgress(taskId: number, date: string, numericValue?: number, booleanValue?: boolean): Observable<void> {
        return this.http.post<void>(`${this.goalsApiUrl}/entries`, { taskId, date, numericValue, booleanValue });
    }

    getDayEntries(assignmentId: number, date: string): Observable<DayEntry[]> {
        const params = new HttpParams().set('date', date);
        return this.http.get<DayEntry[]>(`${this.goalsApiUrl}/my-goals/${assignmentId}/day`, { params });
    }

    saveDayEntries(assignmentId: number, date: string, entries: { taskId: number; numericValue?: number; booleanValue?: boolean }[]): Observable<DayEntry[]> {
        return this.http.post<DayEntry[]>(`${this.goalsApiUrl}/my-goals/${assignmentId}/save-day`, { date, entries });
    }

    unlockEntry(entryId: number): Observable<void> {
        return this.http.delete<void>(`${this.goalsApiUrl}/entries/${entryId}/unlock`);
    }

    // ── Student Report (mentor/admin) ───────────────────────────────────────

    getStudentList(): Observable<StudentSummary[]> {
        return this.http.get<StudentSummary[]>(`${this.reportApiUrl}/students`);
    }

    getStudentReport(studentId: number): Observable<StudentReport> {
        return this.http.get<StudentReport>(`${this.reportApiUrl}/students/${studentId}`);
    }

    approveTask(studentId: number, assignmentId: number, taskId: number, notes?: string): Observable<void> {
        return this.http.post<void>(
            `${this.reportApiUrl}/students/${studentId}/assignments/${assignmentId}/tasks/${taskId}/approve`,
            { notes }
        );
    }

    revokeApproval(studentId: number, assignmentId: number, taskId: number): Observable<void> {
        return this.http.delete<void>(
            `${this.reportApiUrl}/students/${studentId}/assignments/${assignmentId}/tasks/${taskId}/approve`
        );
    }

    getCategoryChart(studentId: number, category: string, startDate?: string, endDate?: string): Observable<DailyProgressEntry[]> {
        let params = new HttpParams().set('category', category);
        if (startDate) params = params.set('startDate', startDate);
        if (endDate) params = params.set('endDate', endDate);
        return this.http.get<DailyProgressEntry[]>(
            `${this.reportApiUrl}/students/${studentId}/chart`, { params }
        );
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

    // ── Class Homework Assignments ────────────────────────────────────────────

    getClassAssignments(classId: number): Observable<any[]> {
        return this.http.get<any[]>(`${environment.apiBaseUrl}/classes/${classId}/assignments`);
    }

    createAssignment(classId: number, request: any): Observable<any> {
        return this.http.post<any>(`${environment.apiBaseUrl}/classes/${classId}/assignments`, request);
    }

    deleteAssignment(classId: number, assignmentId: number): Observable<void> {
        return this.http.delete<void>(`${environment.apiBaseUrl}/classes/${classId}/assignments/${assignmentId}`);
    }
}
