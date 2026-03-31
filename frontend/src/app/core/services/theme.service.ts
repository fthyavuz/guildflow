import { Injectable, signal, effect } from '@angular/core';

export type Theme = 'light' | 'dark';

@Injectable({
    providedIn: 'root'
})
export class ThemeService {
    readonly currentTheme = signal<Theme>(this.getInitialTheme());

    constructor() {
        // Apply immediately to avoid any Angular bootstrap delay or flash
        this.applyTheme(this.currentTheme());

        // Keep attribute and storage in sync with signal
        effect(() => {
            const theme = this.currentTheme();
            this.applyTheme(theme);
        });
    }

    private getInitialTheme(): Theme {
        const saved = localStorage.getItem('guildflow_theme') as Theme | null;
        if (saved) return saved;
        return window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
    }

    private applyTheme(theme: Theme): void {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('guildflow_theme', theme);
    }

    toggleTheme(): void {
        this.currentTheme.update(t => t === 'light' ? 'dark' : 'light');
    }

    setTheme(theme: Theme): void {
        this.currentTheme.set(theme);
    }
}
