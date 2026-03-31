export interface EventResponse {
    id: number;
    title: string;
    description: string;
    startTime: string;
    endTime: string;
    createdById: number;
    createdByName: string;
    educationLevel: string | null;
    targetClassIds: number[];
    targetClassNames: string[];
}

export interface EventParticipantResponse {
    id: number;
    userId: number;
    userName: string;
    role: string;
    isGoing: boolean;
    respondedAt: string;
}

export interface EventAssignmentResponse {
    id: number;
    userId: number;
    userName: string;
    dutyDescription: string;
    assignedAt: string;
}

export interface EventDetailsResponse extends EventResponse {
    participants: EventParticipantResponse[];
    assignments: EventAssignmentResponse[];
    userGoingStatus: boolean | null;
}

export interface EventRequest {
    title: string;
    description: string;
    startTime: string;
    endTime: string;
    targetClassIds?: number[];
}

export interface EventFilterParams {
    filter?: 'UPCOMING' | 'PAST' | 'ALL';
    educationLevel?: string;
}

export interface EventAssignmentRequest {
    userId: number;
    dutyDescription: string;
}

export interface RsvpRequest {
    isGoing: boolean;
}
