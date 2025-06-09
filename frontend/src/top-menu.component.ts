import { Component, EventEmitter, Input, Output } from '@angular/core';
import { User } from './app.component';

@Component({
  selector: 'app-top-menu',
  template: `
    <mat-toolbar color="primary" *ngIf="user">
      <span class="user">Welcome {{user.username}}</span>
      <span class="spacer"></span>
      <button mat-button (click)="logout.emit()">Logout</button>
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
}
