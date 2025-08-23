import { Component, OnInit } from '@angular/core';
import { AuthService } from './auth.service';
import { User } from './app.component';

@Component({
  selector: 'app-user-profile',
  template: `
    <mat-card class="profile-card">
      <mat-card-title>{{ 'PROFILE' | t }}</mat-card-title>
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
      <mat-card-actions class="actions">
        <button mat-raised-button color="primary" (click)="save()">
          <mat-icon>save</mat-icon>
          {{ 'SAVE' | t }}
        </button>
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    .profile-card { max-width: 400px; margin: 1rem auto; padding: 1rem; }
    .actions { display: flex; justify-content: flex-end; }
  `]
})
export class UserProfileComponent implements OnInit {
  username = '';
  password = '';

  constructor(private auth: AuthService) {}

  ngOnInit() {
    this.username = this.auth.user?.username || '';
  }

  save() {
    fetch(`${this.auth.API_URL}/api/users/me`, {
      method: 'PUT',
      headers: this.auth.authHeaders(),
      body: JSON.stringify({ username: this.username, password: this.password || undefined })
    })
      .then(r => r.json())
      .then((u: User) => {
        const oldPwd = atob(this.auth.credentials).split(':')[1] || '';
        const pwd = this.password || oldPwd;
        this.auth.credentials = btoa(`${this.username}:${pwd}`);
        localStorage.setItem('credentials', this.auth.credentials);
        this.auth.user = u;
        localStorage.setItem('user', JSON.stringify(u));
        this.password = '';
      });
  }
}
