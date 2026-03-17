import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
    {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
    },
    {
        path: 'classes',
        canActivate: [authGuard],
        loadComponent: () => import('./features/classes/class-list/class-list.component').then(m => m.ClassListComponent)
    },
    {
        path: 'classes/new',
        canActivate: [authGuard],
        loadComponent: () => import('./features/classes/class-form/class-form.component').then(m => m.ClassFormComponent)
    },
    {
        path: 'classes/edit/:id',
        canActivate: [authGuard],
        loadComponent: () => import('./features/classes/class-form/class-form.component').then(m => m.ClassFormComponent)
    },
    {
        path: 'classes/:id',
        canActivate: [authGuard],
        loadComponent: () => import('./features/classes/class-detail/class-detail.component').then(m => m.ClassDetailComponent)
    },
    {
        path: 'dashboard',
        canActivate: [authGuard],
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
    },
    {
        path: 'mentors',
        canActivate: [authGuard],
        loadComponent: () => import('./features/users/mentor-list/mentor-list.component').then(m => m.MentorListComponent)
    },
    {
        path: 'mentors/new',
        canActivate: [authGuard],
        loadComponent: () => import('./features/users/user-form/user-form.component').then(m => m.UserFormComponent)
    },
    {
        path: 'mentors/edit/:id',
        canActivate: [authGuard],
        loadComponent: () => import('./features/users/user-form/user-form.component').then(m => m.UserFormComponent)
    },
    {
        path: 'students',
        canActivate: [authGuard],
        loadComponent: () => import('./features/users/student-list/student-list.component').then(m => m.StudentListComponent)
    },
    {
        path: 'students/new',
        canActivate: [authGuard],
        loadComponent: () => import('./features/users/user-form/user-form.component').then(m => m.UserFormComponent)
    },
    {
        path: 'students/edit/:id',
        canActivate: [authGuard],
        loadComponent: () => import('./features/users/user-form/user-form.component').then(m => m.UserFormComponent)
    },
    {
        path: 'students/:studentId',
        canActivate: [authGuard],
        loadComponent: () => import('./features/classes/student-profile/student-profile.component').then(m => m.StudentProfileComponent)
    },
    {
        path: 'meetings',
        canActivate: [authGuard],
        loadComponent: () => import('./features/meetings/meeting-list.component').then(m => m.MeetingListComponent)
    },
    {
        path: 'meetings/new',
        canActivate: [authGuard],
        loadComponent: () => import('./features/meetings/meeting-form/meeting-form.component').then(m => m.MeetingFormComponent)
    },
    {
        path: 'goals',
        canActivate: [authGuard],
        loadComponent: () => import('./features/goals/goal-tracking.component').then(m => m.GoalTrackingComponent)
    },

    {
        path: 'events',
        canActivate: [authGuard],
        loadComponent: () => import('./features/events/event-list/event-list.component').then(m => m.EventListComponent)
    },
    {
        path: 'events/new',
        canActivate: [authGuard],
        loadComponent: () => import('./features/events/event-form/event-form.component').then(m => m.EventFormComponent)
    },
    {
        path: 'events/edit/:id',
        canActivate: [authGuard],
        loadComponent: () => import('./features/events/event-form/event-form.component').then(m => m.EventFormComponent)
    },
    {
        path: 'events/:id',
        canActivate: [authGuard],
        loadComponent: () => import('./features/events/event-detail/event-detail.component').then(m => m.EventDetailComponent)
    },
    {
        path: 'goals/new',
        canActivate: [authGuard],
        loadComponent: () => import('./features/goals/goal-form/goal-form.component').then(m => m.GoalFormComponent)
    },
    {
        path: 'goals/edit/:id',
        canActivate: [authGuard],
        loadComponent: () => import('./features/goals/goal-form/goal-form.component').then(m => m.GoalFormComponent)
    },
    {
        path: 'goals/library',
        canActivate: [authGuard],
        loadComponent: () => import('./features/goals/goal-library/goal-library.component').then(m => m.GoalLibraryComponent)
    },
    {
        path: 'goals/assign/:id',
        canActivate: [authGuard],
        loadComponent: () => import('./features/goals/goal-assignment/goal-assignment.component').then(m => m.GoalAssignmentComponent)
    },
    {
        path: 'goals/:id',
        canActivate: [authGuard],
        loadComponent: () => import('./features/goals/goal-tracking.component').then(m => m.GoalTrackingComponent)
    },
    {
        path: 'rooms',
        canActivate: [authGuard],
        loadComponent: () => import('./features/rooms/room-management/room-management.component').then(m => m.RoomManagementComponent)
    },
    {
        path: 'sources',
        canActivate: [authGuard],
        loadComponent: () => import('./features/sources/source-list/source-list.component').then(m => m.SourceListComponent)
    },
    {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
    },
    {
        path: '**',
        redirectTo: 'dashboard'
    }
];
