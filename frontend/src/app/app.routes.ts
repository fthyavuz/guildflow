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
        path: 'users',
        canActivate: [authGuard],
        loadComponent: () => import('./features/users/user-management/user-management.component').then(m => m.UserManagementComponent)
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
        loadComponent: () => import('./features/users/parent-student-management/parent-student-management.component').then(m => m.ParentStudentManagementComponent)
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
        path: 'parents/new',
        canActivate: [authGuard],
        loadComponent: () => import('./features/users/user-form/user-form.component').then(m => m.UserFormComponent)
    },
    {
        path: 'parents/edit/:id',
        canActivate: [authGuard],
        loadComponent: () => import('./features/users/user-form/user-form.component').then(m => m.UserFormComponent)
    },
    {
        path: 'admins/new',
        canActivate: [authGuard],
        loadComponent: () => import('./features/users/user-form/user-form.component').then(m => m.UserFormComponent)
    },
    {
        path: 'admins/edit/:id',
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
        path: 'meetings/edit/:id',
        canActivate: [authGuard],
        loadComponent: () => import('./features/meetings/meeting-form/meeting-form.component').then(m => m.MeetingFormComponent)
    },
    {
        path: 'meetings/:id',
        canActivate: [authGuard],
        loadComponent: () => import('./features/meetings/meeting-detail/meeting-detail.component').then(m => m.MeetingDetailComponent)
    },
    {
        path: 'goals',
        canActivate: [authGuard],
        loadComponent: () => import('./features/goals/homework-list/homework-list.component').then(m => m.HomeworkListComponent)
    },
    {
        path: 'student-report',
        canActivate: [authGuard],
        loadComponent: () => import('./features/student-report/student-report.component').then(m => m.StudentReportComponent)
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
        redirectTo: '/classes'
    },
    {
        path: 'goals/:assignmentId',
        canActivate: [authGuard],
        loadComponent: () => import('./features/goals/homework-detail/homework-detail.component').then(m => m.HomeworkDetailComponent)
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
