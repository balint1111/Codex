import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { User } from './models/user';
import { UserService } from './services/user.service';

@Component({
  selector: 'app-login',
  template: `
    <div class="login">
      <mat-card>
        <mat-card-title>{{ 'LOGIN' | t }}</mat-card-title>
        <mat-card-content>
          <mat-form-field appearance="fill">
            <mat-label>{{ 'USERNAME' | t }}</mat-label>
            <input matInput [(ngModel)]="username">
          </mat-form-field>
          <mat-form-field appearance="fill">
            <mat-label>{{ 'PASSWORD' | t }}</mat-label>
            <input matInput type="password" [(ngModel)]="password">
          </mat-form-field>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" (click)="login()">{{ 'LOGIN' | t }}</button>
          <a mat-button routerLink="/register">{{ 'REGISTER' | t }}</a>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .login { height: calc(100vh - 64px); display: flex; justify-content: center; align-items: center; background: #303030; color: #fff; }
    mat-card { width: 300px; }
  `]
})
export class LoginComponent {
  username = '';
  password = '';

  constructor(
    private auth: AuthService,
    private router: Router,
    private userService: UserService
  ) {}

  login() {
    this.auth.credentials = btoa(`${this.username}:${this.password}`);
    localStorage.setItem('credentials', this.auth.credentials);
    this.userService.getCurrent().subscribe({
      next: (u: User) => {
        this.auth.user = u;
        localStorage.setItem('user', JSON.stringify(u));
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        localStorage.removeItem('credentials');
        alert('Login failed');
      }
    });
  }
}
