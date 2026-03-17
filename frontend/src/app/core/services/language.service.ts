import { Injectable, signal } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

export type SupportedLanguage = 'tr' | 'en' | 'de';

export interface Language {
    code: SupportedLanguage;
    label: string;
    flag: string;
}

@Injectable({
    providedIn: 'root'
})
export class LanguageService {
    readonly supportedLanguages: Language[] = [
        { code: 'tr', label: 'Türkçe', flag: '🇹🇷' },
        { code: 'en', label: 'English', flag: '🇬🇧' },
        { code: 'de', label: 'Deutsch', flag: '🇩🇪' }
    ];

    readonly currentLanguage = signal<SupportedLanguage>('tr');

    constructor(private translate: TranslateService) {
        // Restore previously selected language from localStorage
        const saved = localStorage.getItem('guildflow_lang') as SupportedLanguage | null;
        const initial: SupportedLanguage = saved ?? 'tr';
        this.setLanguage(initial);
    }

    setLanguage(lang: SupportedLanguage): void {
        this.translate.use(lang);
        this.currentLanguage.set(lang);
        localStorage.setItem('guildflow_lang', lang);
        document.documentElement.lang = lang;
    }
}
