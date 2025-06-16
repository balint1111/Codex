import { Component } from '@angular/core';

export interface Privilege { id: number; name: string; }
export interface User { id: number; username: string; privileges: Privilege[]; }

import { AuthService } from './auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  template: `
    <app-top-menu [user]="auth.user" (logout)="logout()"></app-top-menu>
    <mat-sidenav-container *ngIf="auth.user" class="layout">
      <mat-sidenav mode="side" opened class="sidenav">
        <app-navbar [user]="auth.user"></app-navbar>
        <span class="spacer"></span>
        <app-calculator></app-calculator>
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
    .sidenav { display: flex; flex-direction: column; height: 100%; }
    .spacer { flex: 1 1 auto; }
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
