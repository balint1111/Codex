import { Component, EventEmitter, Input, Output } from '@angular/core';
import { User } from './app.component';

@Component({
  selector: 'app-top-menu',
  template: `
    <mat-toolbar color="primary">
      <span *ngIf="user" class="user">Welcome {{user.username}}</span>
      <mat-slide-toggle [(ngModel)]="dark" (change)="toggleDark()">Dark mode</mat-slide-toggle>
      <span class="spacer"></span>
      <button mat-button *ngIf="user" (click)="logout.emit()">Logout</button>
    </mat-toolbar>
  `,
  styles: [`
    .spacer { flex: 1 1 auto; }
    .user { margin-right: 1rem; }
  `]
})
export class TopMenuComponent {
  @Input() user?: User;
  @Output() logout = new EventEmitter<void>();
  dark = localStorage.getItem('darkMode') === 'true';

  toggleDark() {
    localStorage.setItem('darkMode', String(this.dark));
    const darkLink = document.getElementById('dark-theme') as HTMLLinkElement;
    const lightLink = document.getElementById('light-theme') as HTMLLinkElement;
    if (darkLink && lightLink) {
      darkLink.disabled = !this.dark;
      lightLink.disabled = this.dark;
    }
  }
}
