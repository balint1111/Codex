import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { User } from './app.component';

@Component({
  selector: 'app-login',
  template: `
    <div class="login mat-app-background">
      <mat-card>
        <mat-card-title>Login</mat-card-title>
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
          <button mat-raised-button color="primary" (click)="login()">Login</button>
          <a mat-button routerLink="/register">Register</a>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .login {
      height: calc(100vh - 64px);
      display: flex;
      justify-content: center;
      align-items: center;
    }
    mat-card { width: 400px; }
  `]
})
export class LoginComponent {
  username = '';
  password = '';

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
