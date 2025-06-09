import { Component } from '@angular/core';

export interface Privilege { id: number; name: string; }
export interface User { id: number; username: string; privileges: Privilege[]; }

import { AuthService } from './auth.service';

@Component({
  selector: 'app-root',
  template: `
    <h1>Codex Dashboard</h1>
    <app-top-menu [user]="auth.user" (logout)="logout()"></app-top-menu>
    <div *ngIf="!auth.user" class="login">
      <input [(ngModel)]="username" placeholder="username" />
      <input [(ngModel)]="password" placeholder="password" type="password" />
      <button (click)="login()">Login</button>
      <button (click)="register()">Register</button>
    </div>
    <div *ngIf="auth.user" class="layout">
      <app-navbar [user]="auth.user"></app-navbar>
      <section class="content">
        <router-outlet></router-outlet>
      </section>
    </div>
  `,
  styles: [`
    .layout { display: flex; }
    .content { flex: 1; padding: 1rem; }
    .login { margin: 1rem; }
  `]
})
export class AppComponent {
  username = '';
  password = '';

  constructor(public auth: AuthService) {}

  login() {
    this.auth.credentials = btoa(`${this.username}:${this.password}`);
    fetch(`${this.auth.API_URL}/api/users/me`, { headers: { 'Authorization': 'Basic ' + this.auth.credentials } })
      .then(res => {
        if (!res.ok) throw new Error('login failed');
        return res.json();
      })
      .then((u: User) => this.auth.user = u)
      .catch(() => alert('Login failed'));
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
  }
}
