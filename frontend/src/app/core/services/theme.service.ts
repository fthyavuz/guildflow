import { Injectable, signal, effect } from '@angular/core';

export type Theme = 'light' | 'dark';

@Injectable({
    providedIn: 'root'
})
export class ThemeService {
    readonly currentTheme = signal<Theme>('dark');

    constructor() {
        const savedTheme = localStorage.getItem('guildflow_theme') as Theme | null;
        if (savedTheme) {
            this.currentTheme.set(savedTheme);
        } else if (window.matchMedia('(prefers-color-scheme: light)').matches) {
            this.currentTheme.set('light');
        }

        // Apply theme whenever it changes
        effect(() => {
            const theme = this.currentTheme();
            document.documentElement.setAttribute('data-theme', theme);
            localStorage.setItem('guildflow_theme', theme);
        });
    }

    toggleTheme(): void {
        this.currentTheme.update(t => t === 'light' ? 'dark' : 'light');
    }

    setTheme(theme: Theme): void {
        this.currentTheme.set(theme);
    }
}
