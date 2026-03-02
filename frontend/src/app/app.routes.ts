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
        path: 'goals/new',
        canActivate: [authGuard],
        loadComponent: () => import('./features/goals/goal-form/goal-form.component').then(m => m.GoalFormComponent)
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
