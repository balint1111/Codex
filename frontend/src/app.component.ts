import { Component, ViewChild } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';

export interface Privilege { id: number; name: string; }
export interface User { id: number; username: string; privileges: Privilege[]; }

import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { MatSidenav } from '@angular/material/sidenav';

@Component({
  selector: 'app-root',
  template: `
    <app-top-menu
      [user]="auth.user"
      (logout)="logout()"
      (login)="login()"
      (account)="account()"
      (menu)="toggleSidenav()"
      [menuOpened]="sidenav?.opened"
    ></app-top-menu>
    <mat-sidenav-container *ngIf="auth.user" class="layout">
      <mat-sidenav #sidenav [mode]="isMobile ? 'over' : 'side'" [opened]="!isMobile">
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
    :host-context(.light-theme) mat-sidenav {
      background-color: var(--sidebar-bg);
      color: white;
      --mat-list-item-label-text-color: white;
    }
  `]
})
export class AppComponent {
  isMobile = false;
  @ViewChild('sidenav') sidenav?: MatSidenav;

  constructor(
    public auth: AuthService,
    private router: Router,
    private breakpointObserver: BreakpointObserver
  ) {
    this.breakpointObserver
      .observe([Breakpoints.Handset])
      .subscribe(result => (this.isMobile = result.matches));
  }

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

  toggleSidenav() {
    this.sidenav?.toggle();
  }
}
