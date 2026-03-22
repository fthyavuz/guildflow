import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService, SupportedLanguage } from '../../../core/services/language.service';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule, TranslateModule],
    templateUrl: './login.component.html',
    styleUrl: './login.component.css'
})
export class LoginComponent {
    private fb = inject(FormBuilder);
    private authService = inject(AuthService);
    private router = inject(Router);
    readonly languageService = inject(LanguageService);
    readonly themeService = inject(ThemeService);

    loginForm = this.fb.nonNullable.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]]
    });

    error: string | null = null;
    loading = false;

    setLanguage(lang: string): void {
        this.languageService.setLanguage(lang as SupportedLanguage);
    }

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
                this.error = err?.error?.message || 'Invalid email or password';
                this.loading = false;
            }
        });
    }
}
