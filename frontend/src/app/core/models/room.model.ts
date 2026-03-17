export interface Room {
    id: number;
    title: string;
    description?: string;
    capacity: number;
    canStayOvernight: boolean;
    bookings: RoomBooking[];
}

export interface RoomBooking {
    id: number;
    roomId: number;
    bookedById: number;
    bookedByName: string;
    reason: string;
    startTime: string; // ISO date-time string
    endTime: string; // ISO date-time string
}

export interface RoomRequest {
    title: string;
    description?: string;
    capacity: number;
    canStayOvernight: boolean;
}

export interface RoomBookingRequest {
    roomId: number;
    reason: string;
    startTime: string; // ISO date-time string
    endTime: string; // ISO date-time string
}
