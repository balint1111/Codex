import { Component, EventEmitter, Input, Output } from '@angular/core';
import { User } from './app.component';
import { TranslationService, Lang } from './i18n/translation.service';

@Component({
  selector: 'app-top-menu',
  template: `
    <mat-toolbar color="primary">
      <span *ngIf="user" class="user">{{ 'WELCOME' | t:{username: user?.username} }}</span>
      <mat-slide-toggle [(ngModel)]="dark" (change)="toggleDark()">{{ 'DARK_MODE' | t }}</mat-slide-toggle>
      <span class="spacer"></span>
      <mat-select class="lang" [(ngModel)]="lang" (selectionChange)="changeLang($event.value)">
        <mat-option value="en">EN</mat-option>
        <mat-option value="hu">HU</mat-option>
      </mat-select>
      <button mat-button *ngIf="user" (click)="logout.emit()">{{ 'LOGOUT' | t }}</button>
    </mat-toolbar>
  `,
  styles: [`
    .spacer { flex: 1 1 auto; }
    .user { margin-right: 1rem; }
    .lang { width: 60px; margin-right: .5rem; }
  `]
})
export class TopMenuComponent {
  @Input() user?: User;
  @Output() logout = new EventEmitter<void>();
  dark = localStorage.getItem('darkMode') === 'true';
  lang: Lang;

  constructor(private ts: TranslationService) {
    this.lang = this.ts.lang;
  }

  toggleDark() {
    localStorage.setItem('darkMode', String(this.dark));
    const darkLink = document.getElementById('dark-theme') as HTMLLinkElement;
    const lightLink = document.getElementById('light-theme') as HTMLLinkElement;
    if (darkLink && lightLink) {
      darkLink.disabled = !this.dark;
      lightLink.disabled = this.dark;
    }
  }

  changeLang(l: Lang) {
    this.ts.setLanguage(l);
    this.lang = l;
  }
}
