import { Component } from '@angular/core';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  template: `
    <div class="login-container">
      <button mat-raised-button color="primary" (click)="login()">{{ 'LOGIN' | t }}</button>
    </div>
  `,
  styles: [`
    .login-container { height: calc(100vh - 64px); display: flex; justify-content: center; align-items: center; background: linear-gradient(135deg, #1a237e, #303f9f); }
  `]
})
export class LoginComponent {
  constructor(private auth: AuthService) {}

  login() {
    this.auth.login();
  }
}
