import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RoomService } from '../../../core/services/room.service';
import { AuthService } from '../../../core/services/auth.service';
import { Room, RoomBooking, RoomBookingRequest, RoomRequest } from '../../../core/models/room.model';
import { addDays, subDays, startOfDay, format, parseISO } from 'date-fns';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-room-management',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, TranslateModule],
    templateUrl: './room-management.component.html',
    styleUrl: './room-management.component.css'
})
export class RoomManagementComponent implements OnInit {
    private roomService = inject(RoomService);
    public authService = inject(AuthService);
    private fb = inject(FormBuilder);

    user$ = this.authService.currentUser$;
    
    selectedDate: Date = startOfDay(new Date());
    rooms: Room[] = [];
    bookings: RoomBooking[] = [];
    
    // Grid generation (Hours from 08:00 to 22:00)
    timeSlots: number[] = Array.from({length: 15}, (_, i) => i + 8); 

    // UI state
    showRoomForm = false;
    showBookingForm = false;
    
    // Forms
    roomForm: FormGroup;
    bookingForm: FormGroup;
    editingRoomId: number | null = null;
    selectedSlot: { roomId: number, time: number } | null = null;

    constructor() {
        this.roomForm = this.fb.group({
            title: ['', Validators.required],
            description: [''],
            capacity: [10, [Validators.required, Validators.min(1)]],
            canStayOvernight: [false]
        });

        this.bookingForm = this.fb.group({
            reason: ['', Validators.required],
            durationHours: [1, [Validators.required, Validators.min(1), Validators.max(8)]]
        });
    }

    ngOnInit() {
        this.loadData();
    }

    loadData() {
        this.roomService.getAllRooms().subscribe(rooms => {
            this.rooms = rooms;
        });
        
        this.roomService.getBookingsForDate(this.selectedDate).subscribe(bookings => {
            this.bookings = bookings;
        });
    }

    prevDay() {
        this.selectedDate = subDays(this.selectedDate, 1);
        this.loadData();
    }

    nextDay() {
        this.selectedDate = addDays(this.selectedDate, 1);
        this.loadData();
    }

    getFormattedDate(): string {
        return format(this.selectedDate, 'EEEE, MMMM do, yyyy');
    }

    getSlotBooking(roomId: number, hour: number): RoomBooking | undefined {
        const slotStart = new Date(this.selectedDate);
        slotStart.setHours(hour, 0, 0, 0);

        return this.bookings.find(b => {
            if (b.roomId !== roomId) return false;
            const bStart = parseISO(b.startTime);
            const bEnd = parseISO(b.endTime);
            return bStart <= slotStart && bEnd > slotStart;
        });
    }
    
    getSlotClass(roomId: number, hour: number): string {
        const booking = this.getSlotBooking(roomId, hour);
        if (booking) return 'slot-booked';
        
        const slotTime = new Date(this.selectedDate);
        slotTime.setHours(hour, 0, 0, 0);
        if (slotTime < new Date()) return 'slot-past';
        
        return 'slot-free';
    }

    slotClick(roomId: number, hour: number, user: any) {
        const booking = this.getSlotBooking(roomId, hour);
        if (booking) {
            if (user.role === 'ADMIN' || booking.bookedById === user.id) {
                if(confirm(`Cancel booking by ${booking.bookedByName} for ${booking.reason}?`)) {
                    this.deleteBooking(booking.id);
                }
            }
            return;
        }

        const slotTime = new Date(this.selectedDate);
        slotTime.setHours(hour, 0, 0, 0);
        if (slotTime < new Date()) {
            return;
        }

        this.selectedSlot = { roomId, time: hour };
        this.bookingForm.reset({ durationHours: 1 });
        this.showBookingForm = true;
    }

    saveBooking() {
        if (this.bookingForm.invalid || !this.selectedSlot) return;

        const start = new Date(this.selectedDate);
        start.setHours(this.selectedSlot.time, 0, 0, 0);
        
        const end = new Date(start);
        end.setHours(start.getHours() + this.bookingForm.value.durationHours);

        const request: RoomBookingRequest = {
            roomId: this.selectedSlot.roomId,
            reason: this.bookingForm.value.reason,
            startTime: start.toISOString(),
            endTime: end.toISOString()
        };

        this.roomService.bookRoom(request).subscribe({
            next: () => {
                this.showBookingForm = false;
                this.selectedSlot = null;
                this.loadData();
            },
            error: (err) => {
                console.error("Booking error details:", err);
                const msg = err.error?.message || err.message || 'Unknown error occurred';
                alert('Failed to book room: ' + msg);
            }
        });
    }

    deleteBooking(bookingId: number) {
        this.roomService.deleteBooking(bookingId).subscribe(() => {
            this.loadData();
        });
    }

    // Room CRUD
    openRoomForm(room?: Room) {
        if (room) {
            this.editingRoomId = room.id;
            this.roomForm.patchValue(room);
        } else {
            this.editingRoomId = null;
            this.roomForm.reset({ capacity: 10, canStayOvernight: false });
        }
        this.showRoomForm = true;
    }

    saveRoom() {
        if (this.roomForm.invalid) return;
        
        const request: RoomRequest = this.roomForm.value;
        const obs = this.editingRoomId 
            ? this.roomService.updateRoom(this.editingRoomId, request)
            : this.roomService.createRoom(request);

        obs.subscribe(() => {
            this.showRoomForm = false;
            this.loadData();
        });
    }

    deleteRoom(id: number) {
        if(confirm('Are you sure you want to delete this room? This will also remove all its bookings.')) {
            this.roomService.deleteRoom(id).subscribe(() => {
                this.loadData();
            });
        }
    }
}
