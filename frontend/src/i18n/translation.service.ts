import { Injectable } from '@angular/core';
import { translations } from './translations';

export type Lang = 'en' | 'hu';

@Injectable({ providedIn: 'root' })
export class TranslationService {
  lang: Lang = (localStorage.getItem('lang') as Lang) || 'en';

  setLanguage(lang: Lang) {
    this.lang = lang;
    localStorage.setItem('lang', lang);
  }

  translate(key: string, params?: Record<string, string | number>): string {
    const text = (translations as any)[this.lang]?.[key] || key;
    if (params) {
      return text.replace(/\{(\w+)\}/g, (_: string, p: string) => String(params[p] ?? ''));
    }
    return text;
  }
}
