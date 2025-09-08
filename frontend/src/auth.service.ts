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
    try {
      await this.keycloak.updateToken(5);
    } catch (_) {
      this.handleUnauthorized();
      throw _;
    }

    options.headers = { ...(options.headers || {}), ...this.authHeaders() };
    let res = await fetch(url, options);

    if (res.status === 401) {
      try {
        await this.keycloak.updateToken(0);
        options.headers = { ...(options.headers || {}), ...this.authHeaders() };
        res = await fetch(url, options);
      } catch (_) {
        this.handleUnauthorized();
        return res;
      }

      if (res.status === 401) {
        this.handleUnauthorized();
      }
    }

    return res;
  }

  private async loadUser() {
    const res = await this.request(`${this.API_URL}/api/users/me`);
    if (res.ok) {
      this.user = await res.json();
    }
  }

  private handleUnauthorized() {
    this.keycloak.clearToken();
    this.user = undefined;
    this.router.navigate(['/login']);
  }
}
