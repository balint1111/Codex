import { Component } from '@angular/core';

export interface Privilege { id: number; name: string; }
export interface User { id: number; username: string; privileges: Privilege[]; }

import { AuthService } from './auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  template: `
    <app-top-menu [user]="auth.user" (logout)="logout()" (login)="login()" (account)="account()"></app-top-menu>
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
    mat-sidenav {
      background-color: var(--sidebar-bg);
      color: white;
      --mat-list-item-label-text-color: white;
    }
  `]
})
export class AppComponent {
  constructor(public auth: AuthService, private router: Router) {}

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  login() {
    this.auth.login();
  }

  account() {
    this.auth.account();
  }
}
