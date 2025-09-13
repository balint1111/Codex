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
      <button
        mat-icon-button
        *ngIf="user; else loginBtn"
        [matMenuTriggerFor]="userMenu"
      >
        <mat-icon>account_circle</mat-icon>
      </button>
      <mat-menu #userMenu="matMenu">
        <button mat-menu-item (click)="account.emit()">{{ 'MY_ACCOUNT' | t }}</button>
        <button mat-menu-item (click)="logout.emit()">{{ 'LOGOUT' | t }}</button>
      </mat-menu>
      <ng-template #loginBtn>
        <button mat-button (click)="login.emit()">{{ 'LOGIN' | t }}</button>
      </ng-template>
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
  @Output() login = new EventEmitter<void>();
  @Output() account = new EventEmitter<void>();
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
    document.body.classList.toggle('dark-theme', this.dark);
    document.body.classList.toggle('light-theme', !this.dark);
  }

  changeLang(l: Lang) {
    this.ts.setLanguage(l);
    this.lang = l;
  }
}
