import { Injectable } from '@angular/core';
import { User } from './app.component';

@Injectable({ providedIn: 'root' })
export class AuthService {
  user?: User;
  credentials = '';
  API_URL = (window as any).API_URL || 'http://localhost:8080';

  authHeaders() {
    return { 'Authorization': 'Basic ' + this.credentials, 'Content-Type': 'application/json' };
  }
}
