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

export type ProgressEntryStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export type Frequency = 'DAILY' | 'WEEKLY';

export interface TaskProgress {
    taskId: number;
    title: string;
    taskType: 'NUMBER' | 'CHECKBOX';
    targetValue: number;
    currentValue: number;
    progressPercentage: number;
    // Per-entry fields
    entryId?: number;
    entryDate?: string;
    status?: ProgressEntryStatus;
    mentorNotes?: string;
}

export interface GoalProgress {
    goalId: number;
    title: string;
    frequency?: Frequency;
    tasks: TaskProgress[];
    overallProgress: number;
}

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
    goals: GoalProgress[];
}
