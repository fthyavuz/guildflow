import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Room, RoomBooking, RoomRequest, RoomBookingRequest } from '../models/room.model';
import { PagedResponse } from '../models/page.model';
import { environment } from '../../../environments/environment';
import { format } from 'date-fns';

@Injectable({
    providedIn: 'root'
})
export class RoomService {
    private http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiBaseUrl}/rooms`;

    getAllRooms(): Observable<Room[]> {
        return this.http.get<PagedResponse<Room>>(this.apiUrl)
            .pipe(map(res => res.content));
    }

    getRoomById(id: number): Observable<Room> {
        return this.http.get<Room>(`${this.apiUrl}/${id}`);
    }

    createRoom(room: RoomRequest): Observable<Room> {
        return this.http.post<Room>(this.apiUrl, room);
    }

    updateRoom(id: number, room: RoomRequest): Observable<Room> {
        return this.http.put<Room>(`${this.apiUrl}/${id}`, room);
    }

    deleteRoom(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    getBookingsForDate(date: Date): Observable<RoomBooking[]> {
        const formattedDate = format(date, 'yyyy-MM-dd');
        const params = new HttpParams().set('date', formattedDate);
        return this.http.get<RoomBooking[]>(`${this.apiUrl}/bookings`, { params });
    }

    bookRoom(booking: RoomBookingRequest): Observable<RoomBooking> {
        return this.http.post<RoomBooking>(`${this.apiUrl}/bookings`, booking);
    }

    deleteBooking(bookingId: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/bookings/${bookingId}`);
    }
}
