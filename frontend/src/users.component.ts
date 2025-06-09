import { Component, OnInit } from '@angular/core';
import { AuthService } from './auth.service';
import { Privilege, User } from './app.component';

@Component({
  selector: 'app-users',
  template: `
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
  `
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  allPrivileges: Privilege[] = [];

  constructor(private auth: AuthService) {}

  ngOnInit() {
    this.loadUsers();
  }

  private authHeaders() {
    return this.auth.authHeaders();
  }

  loadUsers() {
    fetch(`${this.auth.API_URL}/api/users`, { headers: this.authHeaders() })
      .then(r => r.json())
      .then((d: User[]) => this.users = d);
    if (this.allPrivileges.length === 0) {
      fetch(`${this.auth.API_URL}/api/privileges`, { headers: this.authHeaders() })
        .then(r => r.json())
        .then((p: Privilege[]) => this.allPrivileges = p);
    }
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
    fetch(`${this.auth.API_URL}/api/users/${u.id}/privileges`, {
      method: 'POST',
      headers: this.authHeaders(),
      body: JSON.stringify(u.privileges.map(pr => pr.id))
    });
  }

  deleteUser(id: number) {
    fetch(`${this.auth.API_URL}/api/users/${id}`, { method: 'DELETE', headers: this.authHeaders() })
      .then(() => this.loadUsers());
  }
}
