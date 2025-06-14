import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { User } from './models/user';
import { UserService } from './services/user.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  user?: User;
  credentials = '';
  API_URL = (window as any).API_URL || 'http://localhost:8081';

  constructor(private http: HttpClient, private userService: UserService) {
    const creds = localStorage.getItem('credentials');
    if (creds) {
      this.credentials = creds;
      this.userService.getCurrent().subscribe(u => this.user = u, () => {});
    }
  }

  authHeaders() {
    return { 'Authorization': 'Basic ' + this.credentials, 'Content-Type': 'application/json' };
  }
}
