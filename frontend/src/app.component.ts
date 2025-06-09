import { Component } from '@angular/core';

export interface Privilege { id: number; name: string; }
export interface User { id: number; username: string; privileges: Privilege[]; }

import { AuthService } from './auth.service';

@Component({
  selector: 'app-root',
  template: `
    <app-top-menu [user]="auth.user" (logout)="logout()"></app-top-menu>
    <div *ngIf="!auth.user" class="login">
      <mat-form-field appearance="fill">
        <mat-label>Username</mat-label>
        <input matInput [(ngModel)]="username" />
      </mat-form-field>
      <mat-form-field appearance="fill">
        <mat-label>Password</mat-label>
        <input matInput [(ngModel)]="password" type="password" />
      </mat-form-field>
      <div class="buttons">
        <button mat-raised-button color="primary" (click)="login()">Login</button>
        <button mat-button (click)="register()">Register</button>
      </div>
    </div>
    <mat-sidenav-container *ngIf="auth.user" class="layout">
      <mat-sidenav mode="side" opened>
        <app-navbar [user]="auth.user"></app-navbar>
      </mat-sidenav>
      <mat-sidenav-content class="content">
        <router-outlet></router-outlet>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .layout { height: calc(100vh - 64px); }
    .login { padding: 1rem; display: flex; flex-direction: column; gap: 0.5rem; max-width: 300px; }
    .buttons { display: flex; gap: 0.5rem; }
  `]
})
export class AppComponent {
  username = '';
  password = '';

  constructor(public auth: AuthService) {}

  login() {
    this.auth.credentials = btoa(`${this.username}:${this.password}`);
    localStorage.setItem('credentials', this.auth.credentials);
    fetch(`${this.auth.API_URL}/api/users/me`, { headers: { 'Authorization': 'Basic ' + this.auth.credentials } })
      .then(res => {
        if (!res.ok) throw new Error('login failed');
        return res.json();
      })
      .then((u: User) => {
        this.auth.user = u;
        localStorage.setItem('user', JSON.stringify(u));
      })
      .catch(() => {
        localStorage.removeItem('credentials');
        alert('Login failed');
      });
  }

  register() {
    const body = `username=${encodeURIComponent(this.username)}&password=${encodeURIComponent(this.password)}`;
    fetch(`${this.auth.API_URL}/api/users/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body
    }).then(() => alert('Registered'));
  }

  logout() {
    this.auth.user = undefined;
    this.auth.credentials = '';
    localStorage.removeItem('credentials');
    localStorage.removeItem('user');
  }
}
