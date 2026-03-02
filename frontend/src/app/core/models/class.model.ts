export interface MentorClass {
    id: number;
    name: string;
    description: string;
    educationLevel: string;
    mentorId: number;
    active: boolean;
}

export interface ClassResponse {
    id: number;
    name: string;
    description: string;
    educationLevel: string;
    mentorName: string;
    mentorId: number;
    studentCount: number;
}
