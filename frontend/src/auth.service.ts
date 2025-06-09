import { Injectable } from '@angular/core';
import { User } from './app.component';

@Injectable({ providedIn: 'root' })
export class AuthService {
  user?: User;
  credentials = '';
  API_URL = (window as any).API_URL || 'http://localhost:8080';

  constructor() {
    const creds = localStorage.getItem('credentials');
    if (creds) {
      this.credentials = creds;
      fetch(`${this.API_URL}/api/users/me`, { headers: this.authHeaders() })
        .then(r => r.ok ? r.json() : undefined)
        .then(u => this.user = u);
    }
  }

  authHeaders() {
    return { 'Authorization': 'Basic ' + this.credentials, 'Content-Type': 'application/json' };
  }
}
