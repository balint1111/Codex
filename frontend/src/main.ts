import { Component, NgModule } from '@angular/core';
import '@angular/compiler';
import 'zone.js';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from "@angular/forms";
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

interface Privilege { id: number; name: string; }
interface User { id: number; username: string; privileges: Privilege[]; }

const API_URL = (window as any).API_URL || 'http://localhost:8080';

@Component({
  selector: 'app-root',
  template: `
    <h1>Codex Dashboard</h1>
    <div *ngIf="user" class="top">
      Welcome {{user.username}} | <a (click)="logout()" href="#">Logout</a>
    </div>
    <div *ngIf="!user" class="login">
      <input [(ngModel)]="username" placeholder="username" />
      <input [(ngModel)]="password" placeholder="password" type="password" />
      <button (click)="login()">Login</button>
      <button (click)="register()">Register</button>
    </div>
    <div *ngIf="user" class="layout">
      <nav>
        <ul>
          <li *ngIf="hasPrivilege('dashboard')" (click)="view='dashboard'">Dashboard</li>
          <li *ngIf="hasPrivilege('users')" (click)="view='users'; loadUsers()">Users</li>
        </ul>
      </nav>
      <section>
        <div *ngIf="view==='dashboard'">
          <p>Welcome to the dashboard.</p>
        </div>
        <div *ngIf="view==='users'">
          <table>
            <tr>
              <th>User</th>
              <th *ngFor="let p of allPrivileges">{{p.name}}</th>
              <th>Actions</th>
            </tr>
            <tr *ngFor="let u of users">
              <td>{{u.username}}</td>
              <td *ngFor="let p of allPrivileges">
                <input type="checkbox" [checked]="hasUserPrivilege(u,p)" (change)="toggleUserPrivilege(u,p,$event)"/>
              </td>
              <td><button (click)="deleteUser(u.id)">Delete</button></td>
            </tr>
          </table>
        </div>
      </section>
    </div>
  `
})
class AppComponent {
  user?: User;
  credentials = '';
  users: User[] = [];
  allPrivileges: Privilege[] = [];
  view: 'dashboard' | 'users' = 'dashboard';

  username = '';
  password = '';

  private authHeaders() {
    return { 'Authorization': 'Basic ' + this.credentials, 'Content-Type': 'application/json' };
  }

  hasPrivilege(name: string): boolean {
    return this.user?.privileges.some(p => p.name === name) ?? false;
  }

  hasUserPrivilege(u: User, p: Privilege): boolean {
    return u.privileges.some(pp => pp.id === p.id);
  }

  toggleUserPrivilege(u: User, p: Privilege, ev: Event) {
    const checked = (ev.target as HTMLInputElement).checked;
    if (checked) {
      if (!this.hasUserPrivilege(u, p)) u.privileges.push(p);
    } else {
      u.privileges = u.privileges.filter(pp => pp.id !== p.id);
    }
    fetch(`${API_URL}/api/users/${u.id}/privileges`, {
      method: 'POST',
      headers: this.authHeaders(),
      body: JSON.stringify(u.privileges.map(pr => pr.id))
    });
  }

  loadUsers() {
    fetch(`${API_URL}/api/users`, { headers: this.authHeaders() })
      .then(r => r.json())
      .then((d: User[]) => this.users = d);
    if (this.allPrivileges.length === 0) {
      fetch(`${API_URL}/api/privileges`, { headers: this.authHeaders() })
        .then(r => r.json())
        .then((p: Privilege[]) => this.allPrivileges = p);
    }
  }

  login() {
    this.credentials = btoa(`${this.username}:${this.password}`);
    fetch(`${API_URL}/api/users/me`, { headers: { 'Authorization': 'Basic ' + this.credentials } })
      .then(res => {
        if (!res.ok) throw new Error('login failed');
        return res.json();
      })
      .then((u: User) => {
        this.user = u;
        this.loadUsers();
      })
      .catch(() => alert('Login failed'));
  }

  register() {
    const body = `username=${encodeURIComponent(this.username)}&password=${encodeURIComponent(this.password)}`;
    fetch(`${API_URL}/api/users/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body
    }).then(() => alert('Registered'));
  }

  deleteUser(id: number) {
    fetch(`${API_URL}/api/users/${id}`, { method: 'DELETE', headers: this.authHeaders() })
      .then(() => this.loadUsers());
  }

  logout() {
    this.user = undefined;
    this.users = [];
    this.credentials = '';
  }
}

@NgModule({
  declarations: [AppComponent],
  imports: [BrowserModule, FormsModule],
  bootstrap: [AppComponent]
})
class AppModule {}

platformBrowserDynamic().bootstrapModule(AppModule);
