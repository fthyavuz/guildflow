import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { ClassService } from '../../core/services/class.service';
import { GoalService } from '../../core/services/goal.service';
import { MeetingService } from '../../core/services/meeting.service';
import { Router, RouterModule } from '@angular/router';
import { take, switchMap, of, combineLatest, map, shareReplay } from 'rxjs';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './dashboard.component.html',
    styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
    private authService = inject(AuthService);
    private router = inject(Router);
    private goalService = inject(GoalService);
    private classService = inject(ClassService);
    private meetingService = inject(MeetingService);

    today = new Date();
    user$ = this.authService.currentUser$.pipe(shareReplay(1));

    dashboardData$ = this.user$.pipe(
        switchMap(user => {
            if (!user) return of(null);

            const common = {
                meetings: this.meetingService.getMyMeetings().pipe(
                    map(ms => ms.slice(0, 3)) // Last 3 meetings
                )
            };

            if (user.role === 'ADMIN') {
                return combineLatest({
                    ...common,
                    classes: this.classService.getClasses(),
                    stats: this.classService.getSystemStats()
                });
            } else if (user.role === 'MENTOR') {
                return combineLatest({
                    ...common,
                    classes: this.classService.getClasses()
                });
            } else if (user.role === 'STUDENT') {
                return combineLatest({
                    ...common,
                    goals: this.goalService.getMyGoals()
                });
            }
            return of(null);
        })
    );

    logout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
