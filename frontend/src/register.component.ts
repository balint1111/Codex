import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-register',
  template: `
    <div class="register-container">
      <mat-card class="register-card mat-elevation-z8">
        <mat-card-title class="title">{{ 'REGISTER' | t }}</mat-card-title>
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
          <button mat-raised-button color="primary" (click)="register()">{{ 'REGISTER' | t }}</button>
          <a mat-button routerLink="/login">{{ 'BACK_TO_LOGIN' | t }}</a>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .register-container { height: calc(100vh - 64px); display: flex; justify-content: center; align-items: center; background: linear-gradient(135deg, #1a237e, #303f9f); }
    .register-card { width: 400px; padding: 32px; }
    .title { text-align: center; margin-bottom: 16px; }
    mat-form-field { width: 100%; margin-bottom: 16px; }
    .actions { display: flex; justify-content: space-between; }
  `]
})
export class RegisterComponent {
  username = '';
  password = '';
  hidePassword = true;

  constructor(private auth: AuthService, private router: Router) {}

  register() {
    const body = `username=${encodeURIComponent(this.username)}&password=${encodeURIComponent(this.password)}`;
    fetch(`${this.auth.API_URL}/api/users/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body
    }).then(() => {
      alert('Registered');
      this.router.navigate(['/login']);
    });
  }
}
