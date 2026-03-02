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

export interface TaskProgress {
    taskId: number;
    title: string;
    taskType: 'NUMBER' | 'CHECKBOX'; // Corrected per user's fix
    targetValue: number;
    currentValue: number;
    progressPercentage: number;
}

export interface GoalProgress {
    goalId: number;
    title: string;
    tasks: TaskProgress[];
    overallProgress: number;
}

export interface StudentProfile {
    student: User;
    currentClass?: ClassResponse;
    evaluations: EvaluationResponse[];
    goals: GoalProgress[];
}
