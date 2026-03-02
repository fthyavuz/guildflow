import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './login.component.html',
    styleUrl: './login.component.css'
})
export class LoginComponent {
    private fb = inject(FormBuilder);
    private authService = inject(AuthService);
    private router = inject(Router);

    loginForm = this.fb.nonNullable.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]]
    });

    error: string | null = null;
    loading = false;

    onSubmit(): void {
        if (this.loginForm.invalid) return;

        this.loading = true;
        this.error = null;

        const { email, password } = this.loginForm.getRawValue();

        this.authService.login({ email, password }).subscribe({
            next: () => {
                this.router.navigate(['/dashboard']);
            },
            error: (err: any) => {
                this.error = 'Invalid email or password';
                this.loading = false;
                console.error('Login error', err);
            }
        });
    }
}
