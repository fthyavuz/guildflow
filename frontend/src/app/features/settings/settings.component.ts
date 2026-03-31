import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService, Theme } from '../../core/services/theme.service';
import { LanguageService, SupportedLanguage } from '../../core/services/language.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, TranslateModule, ReactiveFormsModule],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css'
})
export class SettingsComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  public themeService = inject(ThemeService);
  public languageService = inject(LanguageService);
  public translate = inject(TranslateService);
  private notificationService = inject(NotificationService);

  passwordForm: FormGroup;
  isSubmitting = false;

  constructor() {
    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  setTheme(theme: Theme) {
     this.themeService.setTheme(theme);
  }

  setLanguage(lang: string) {
     this.languageService.setLanguage(lang as SupportedLanguage);
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { 'mismatch': true };
  }

  onSubmitPassword() {
    if (this.passwordForm.invalid) return;
    
    this.isSubmitting = true;
    const { currentPassword, newPassword } = this.passwordForm.value;
    
    this.authService.changePassword({ currentPassword, newPassword }).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.passwordForm.reset();
        const successMsg = this.translate.instant('SETTINGS.PASSWORD.SUCCESS');
        this.notificationService.success(successMsg);
      },
      error: (err) => {
        this.isSubmitting = false;
        const msg = err.error?.message || 'Error updating password';
        this.notificationService.error(msg);
      }
    });
  }
}
