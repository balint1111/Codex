import { Component, Input } from '@angular/core';
import { User } from './app.component';

@Component({
  selector: 'app-navbar',
  template: `
    <mat-nav-list>
      <a mat-list-item routerLink="/dashboard" routerLinkActive="active" *ngIf="hasPrivilege('dashboard')">Dashboard</a>
      <a mat-list-item routerLink="/users" routerLinkActive="active" *ngIf="hasPrivilege('users')">Users</a>
    </mat-nav-list>
  `,
  styles: [`
    .active { font-weight: bold; }
  `]
})
export class NavbarComponent {
  @Input() user?: User;

  hasPrivilege(name: string): boolean {
    return this.user?.privileges.some(p => p.name === name) ?? false;
  }
}
