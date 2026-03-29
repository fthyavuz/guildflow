export interface MeetingResponse {
    id: number;
    classId: number;
    className: string;
    mentorName?: string;
    title: string;
    description: string;
    startTime: string;
    endTime: string;
    location: string;
    recurring: boolean;
    recurrenceGroupId?: string;
    roomId?: number;
    roomTitle?: string;
    roomBookingId?: number;
}

export interface MeetingRequest {
    classId: number;
    title: string;
    description?: string;
    startTime: string;
    endTime: string;
    location?: string;
    recurring: boolean;
    roomId?: number | null;
}

export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'EXCUSED' | 'LATE';

export interface AttendanceRecord {
    id: number;
    studentId: number;
    studentName: string;
    status: AttendanceStatus;
    note?: string;
    recordedAt?: string;
}

export interface MeetingUpdateRequest {
    title?: string;
    description?: string;
    startTime?: string;
    endTime?: string;
    location?: string;
    roomId?: number | null;
}
