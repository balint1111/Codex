import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { UserService } from './services/user.service';

@Component({
  selector: 'app-register',
  template: `
    <div class="register">
      <mat-card>
        <mat-card-title>{{ 'REGISTER' | t }}</mat-card-title>
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
          <button mat-raised-button color="primary" (click)="register()">{{ 'REGISTER' | t }}</button>
          <a mat-button routerLink="/login">{{ 'BACK_TO_LOGIN' | t }}</a>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .register { height: calc(100vh - 64px); display: flex; justify-content: center; align-items: center; background: #303030; color: #fff; }
    mat-card { width: 300px; }
  `]
})
export class RegisterComponent {
  username = '';
  password = '';

  constructor(private auth: AuthService, private router: Router, private userService: UserService) {}

  register() {
    this.userService.register(this.username, this.password).subscribe(() => {
      alert('Registered');
      this.router.navigate(['/login']);
    });
  }
}
