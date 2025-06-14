import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../auth.service';
import { User } from '../models/user';
import { Privilege } from '../models/privilege';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class UserService extends ApiService<User> {
  constructor(http: HttpClient, auth: AuthService) {
    super(http, auth, 'api/users');
  }

  /** Get the currently authenticated user. */
  getCurrent(): Observable<User> {
    return this.http.get<User>(`${this.auth.API_URL}/api/users/me`, {
      headers: this.auth.authHeaders()
    });
  }

  register(username: string, password: string): Observable<void> {
    const body = `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`;
    return this.http.post<void>(`${this.auth.API_URL}/api/users/register`, body, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });
  }

  updatePrivileges(userId: number, privileges: Privilege[] | number[]): Observable<void> {
    const ids = privileges.map(p => typeof p === 'number' ? p : p.id);
    return this.http.post<void>(`${this.auth.API_URL}/api/users/${userId}/privileges`, ids, {
      headers: this.auth.authHeaders()
    });
  }
}
