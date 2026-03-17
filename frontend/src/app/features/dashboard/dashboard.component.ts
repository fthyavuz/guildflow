import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { ClassService } from '../../core/services/class.service';
import { GoalService } from '../../core/services/goal.service';
import { MeetingService } from '../../core/services/meeting.service';
import { EventService } from '../../core/services/event.service';
import { Router, RouterModule } from '@angular/router';
import { take, switchMap, of, combineLatest, map, shareReplay } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService, SupportedLanguage } from '../../core/services/language.service';
import { ThemeService } from '../../core/services/theme.service';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, RouterModule, TranslateModule],
    templateUrl: './dashboard.component.html',
    styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
    private authService = inject(AuthService);
    private router = inject(Router);
    private goalService = inject(GoalService);
    private classService = inject(ClassService);
    private meetingService = inject(MeetingService);
    private eventService = inject(EventService);
    readonly languageService = inject(LanguageService);
    readonly themeService = inject(ThemeService);

    today = new Date();
    user$ = this.authService.currentUser$.pipe(shareReplay(1));

    dashboardData$ = this.user$.pipe(
        switchMap(user => {
            if (!user) return of(null);

            const common = {
                meetings: this.meetingService.getMyMeetings().pipe(
                    map(ms => ms.slice(0, 3)) // Last 3 meetings
                ),
                events: this.eventService.getUpcomingEvents().pipe(
                    map(es => es.slice(0, 3)) // Last 3 events
                )
            };

            if (user.role === 'ADMIN') {
                return combineLatest({
                    ...common,
                    classes: this.classService.getClasses(),
                    stats: this.classService.getSystemStats(),
                    goals: of([])
                });
            } else if (user.role === 'MENTOR') {
                return combineLatest({
                    ...common,
                    classes: this.classService.getClasses(),
                    stats: of(null),
                    goals: of([])
                });
            } else if (user.role === 'STUDENT') {
                return combineLatest({
                    ...common,
                    classes: of([]),
                    stats: of(null),
                    goals: this.goalService.getMyGoals()
                });
            }

            return combineLatest({
                ...common,
                classes: of([]),
                stats: of(null),
                goals: of([])
            });
        })
    );

    setLanguage(lang: string): void {
        this.languageService.setLanguage(lang as SupportedLanguage);
    }

    logout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
