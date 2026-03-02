export interface MeetingResponse {
    id: number;
    classId: number;
    className: string;
    title: string;
    description: string;
    startTime: string;
    endTime: string;
    location: string;
    recurring: boolean;
    recurrenceGroupId?: string;
}

export interface MeetingRequest {
    classId: number;
    title: string;
    description: string;
    startTime: string;
    endTime: string;
    location: string;
    recurring: boolean;
}
