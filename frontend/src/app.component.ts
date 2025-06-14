import { Component } from '@angular/core';
import { User } from './models/user';

import { AuthService } from './auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  template: `
    <app-top-menu [user]="auth.user" (logout)="logout()"></app-top-menu>
    <mat-sidenav-container *ngIf="auth.user" class="layout">
      <mat-sidenav mode="side" opened>
        <app-navbar [user]="auth.user"></app-navbar>
      </mat-sidenav>
      <mat-sidenav-content class="content">
        <router-outlet></router-outlet>
      </mat-sidenav-content>
    </mat-sidenav-container>
    <div *ngIf="!auth.user">
      <router-outlet></router-outlet>
    </div>
  `,
  styles: [`
    .layout { height: calc(100vh - 64px); }
  `]
})
export class AppComponent {
  constructor(public auth: AuthService, private router: Router) {}

  logout() {
    this.auth.user = undefined;
    this.auth.credentials = '';
    localStorage.removeItem('credentials');
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }
}
