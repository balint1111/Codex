import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-register',
  template: `
    <div class="register mat-app-background">
      <mat-card>
        <mat-card-title>Register</mat-card-title>
        <mat-card-content>
          <mat-form-field appearance="fill">
            <mat-label>Username</mat-label>
            <input matInput [(ngModel)]="username">
          </mat-form-field>
          <mat-form-field appearance="fill">
            <mat-label>Password</mat-label>
            <input matInput type="password" [(ngModel)]="password">
          </mat-form-field>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" (click)="register()">Register</button>
          <a mat-button routerLink="/login">Back to Login</a>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .register {
      height: calc(100vh - 64px);
      display: flex;
      justify-content: center;
      align-items: center;
    }
    mat-card { width: 400px; }
  `]
})
export class RegisterComponent {
  username = '';
  password = '';

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
