import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { Router } from '@angular/router';
import { User } from './app.component';

@Injectable({ providedIn: 'root' })
export class AuthService {
  user?: User;
  API_URL = (window as any).API_URL || 'http://localhost:8081';
  keycloak: any;

  constructor(private router: Router) {
    this.keycloak = new Keycloak({
      url: (window as any).KEYCLOAK_URL || 'http://localhost:30001',
      realm: (window as any).KEYCLOAK_REALM || 'codex',
      clientId: (window as any).KEYCLOAK_CLIENT_ID || 'codex-frontend'
    });
    this.keycloak.init({ onLoad: 'check-sso' }).then((authenticated: boolean) => {
      if (authenticated) {
        this.loadUser();
      }
    });
  }

  login() {
    this.keycloak.login();
  }

  logout() {
    this.user = undefined;
    this.keycloak.logout();
  }

  private authHeaders() {
    return { 'Authorization': 'Bearer ' + this.keycloak.token, 'Content-Type': 'application/json' };
  }

  async request(url: string, options: RequestInit = {}) {
    options.headers = { ...(options.headers || {}), ...this.authHeaders() };
    const res = await fetch(url, options);
    if (res.status === 401) {
      this.keycloak.clearToken();
      this.user = undefined;
      this.router.navigate(['/login']);
    }
    return res;
  }

  private async loadUser() {
    const res = await this.request(`${this.API_URL}/api/users/me`);
    if (res.ok) {
      this.user = await res.json();
    }
  }
}
