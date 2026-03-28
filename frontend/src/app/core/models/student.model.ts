import { User } from './auth.model';
import { ClassResponse } from './class.model';

export interface EvaluationResponse {
    id: number;
    mentorId: number;
    mentorName: string;
    periodName: string;
    content: string;
    createdAt: string;
}

export type Frequency = 'DAILY' | 'WEEKLY';

// ── Homework list (student) ────────────────────────────────────────────────

export interface HomeworkSummary {
    assignmentId: number;
    title: string;
    description?: string;
    startDate: string;
    endDate: string;
    frequency?: Frequency;
    taskCount: number;
    overallProgress: number;
}

// ── Day entry (student data entry) ────────────────────────────────────────

export interface DayEntry {
    taskId: number;
    title: string;
    taskType: 'NUMBER' | 'CHECKBOX';
    targetValue?: number;
    cumulativeValue: number;
    entryId?: number;
    numericEntry?: number;
    booleanEntry?: boolean;
    dayLocked: boolean;
    donePermanently: boolean;
}

// ── Student Report (mentor/admin) ─────────────────────────────────────────

export interface TaskReport {
    taskId: number;
    title: string;
    taskType: 'NUMBER' | 'CHECKBOX';
    targetValue?: number;
    currentValue: number;
    progressPercentage: number;
    approved: boolean;
    approvedAt?: string;
    approvedByName?: string;
    approverNotes?: string;
}

export interface AssignmentReport {
    assignmentId: number;
    title: string;
    startDate: string;
    endDate: string;
    frequency?: string;
    tasks: TaskReport[];
}

export interface StudentReport {
    studentId: number;
    firstName: string;
    lastName: string;
    email: string;
    assignments: AssignmentReport[];
}

// ── Legacy (kept for student-profile component) ───────────────────────────

export interface PendingProgressEntry {
    entryId: number;
    taskId: number;
    taskTitle: string;
    taskType: 'NUMBER' | 'CHECKBOX';
    goalId: number;
    goalTitle: string;
    studentId: number;
    studentName: string;
    entryDate: string;
    numericValue?: number;
    booleanValue?: boolean;
    submittedAt: string;
}

export interface StudentProfile {
    student: User;
    currentClass?: ClassResponse;
    evaluations: EvaluationResponse[];
    goals: HomeworkSummary[];
}
