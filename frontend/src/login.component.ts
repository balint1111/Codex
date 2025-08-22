import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { User } from './app.component';

@Component({
  selector: 'app-login',
  template: `
    <div class="login-container">
      <mat-card class="login-card mat-elevation-z8">
        <mat-card-title class="title">{{ 'LOGIN' | t }}</mat-card-title>
        <mat-card-content>
          <mat-form-field appearance="outline">
            <mat-label>{{ 'USERNAME' | t }}</mat-label>
            <mat-icon matPrefix>person</mat-icon>
            <input matInput [(ngModel)]="username">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>{{ 'PASSWORD' | t }}</mat-label>
            <mat-icon matPrefix>lock</mat-icon>
            <input matInput [type]="hidePassword ? 'password' : 'text'" [(ngModel)]="password">
            <button mat-icon-button matSuffix type="button" (click)="hidePassword = !hidePassword">
              <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
            </button>
          </mat-form-field>
        </mat-card-content>
        <mat-card-actions class="actions">
          <button mat-raised-button color="primary" (click)="login()">{{ 'LOGIN' | t }}</button>
          <a mat-button routerLink="/register">{{ 'REGISTER' | t }}</a>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container { height: calc(100vh - 64px); display: flex; justify-content: center; align-items: center; background: linear-gradient(135deg, #1a237e, #303f9f); }
    .login-card { width: 320px; padding: 24px; }
    .title { text-align: center; margin-bottom: 16px; }
    mat-form-field { width: 100%; margin-bottom: 16px; }
    .actions { display: flex; justify-content: space-between; }
  `]
})
export class LoginComponent {
  username = '';
  password = '';
  hidePassword = true;

  constructor(private auth: AuthService, private router: Router) {}

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
        this.router.navigate(['/dashboard']);
      })
      .catch(() => {
        localStorage.removeItem('credentials');
        alert('Login failed');
      });
  }
}
