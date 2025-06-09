import { Component, Input } from '@angular/core';
import { User } from './app.component';

@Component({
  selector: 'app-navbar',
  template: `
    <nav class="navbar">
      <ul>
        <li *ngIf="hasPrivilege('dashboard')"><a routerLink="/dashboard" routerLinkActive="active">Dashboard</a></li>
        <li *ngIf="hasPrivilege('users')"><a routerLink="/users" routerLinkActive="active">Users</a></li>
      </ul>
    </nav>
  `,
  styles: [`
    .navbar { width: 200px; background: #f0f0f0; padding: 1rem; }
    .navbar ul { list-style: none; padding: 0; }
    .navbar li { margin: 0.5rem 0; }
    .active { font-weight: bold; }
  `]
})
export class NavbarComponent {
  @Input() user?: User;

  hasPrivilege(name: string): boolean {
    return this.user?.privileges.some(p => p.name === name) ?? false;
  }
}
