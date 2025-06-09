import { Component, EventEmitter, Input, Output } from '@angular/core';
import { User } from './app.component';

@Component({
  selector: 'app-top-menu',
  template: `
    <div class="top-menu" *ngIf="user">
      <span>Welcome {{user.username}}</span>
      <button (click)="logout.emit()">Logout</button>
    </div>
  `,
  styles: [`
    .top-menu { background: #333; color: white; padding: 10px; display: flex; justify-content: flex-end; align-items: center; }
    .top-menu span { flex: 1; }
    .top-menu button { margin-left: 10px; }
  `]
})
export class TopMenuComponent {
  @Input() user?: User;
  @Output() logout = new EventEmitter<void>();
}
