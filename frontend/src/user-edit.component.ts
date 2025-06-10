import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from './auth.service';
import { User, Privilege } from './app.component';

@Component({
  selector: 'app-user-edit',
  template: `
    <mat-card *ngIf="user" class="user-card">
      <mat-card-title>{{ 'EDIT_USER' | t:{username: user?.username} }}</mat-card-title>
      <mat-card-content>
        <div class="priv" *ngFor="let p of allPrivileges">
          <mat-checkbox [checked]="hasPrivilege(p)" (change)="togglePrivilege(p, $event)">
            {{p.name}}
          </mat-checkbox>
        </div>
      </mat-card-content>
      <mat-card-actions>
        <button mat-stroked-button color="primary" routerLink="/users">
          <mat-icon>arrow_back</mat-icon>
          {{ 'BACK' | t }}
        </button>
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    .priv { display: block; margin: 0.25rem 0; }
    .user-card { max-width: 400px; margin: 1rem auto; padding: 1rem; }
    mat-card-title { margin-bottom: 0.5rem; }
  `]
})
export class UserEditComponent implements OnInit {
  user?: User;
  allPrivileges: Privilege[] = [];

  constructor(private route: ActivatedRoute, private auth: AuthService) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    fetch(`${this.auth.API_URL}/api/users/${id}`, { headers: this.auth.authHeaders() })
      .then(r => r.json())
      .then((u: User) => this.user = u);
    fetch(`${this.auth.API_URL}/api/privileges`, { headers: this.auth.authHeaders() })
      .then(r => r.json())
      .then((p: Privilege[]) => this.allPrivileges = p);
  }

  hasPrivilege(p: Privilege) {
    return this.user?.privileges.some(pr => pr.id === p.id);
  }

  togglePrivilege(p: Privilege, event: any) {
    if (!this.user) return;
    const checked = event.checked;
    if (checked) {
      if (!this.hasPrivilege(p)) this.user.privileges.push(p);
    } else {
      this.user.privileges = this.user.privileges.filter(pr => pr.id !== p.id);
    }
    fetch(`${this.auth.API_URL}/api/users/${this.user.id}/privileges`, {
      method: 'POST',
      headers: this.auth.authHeaders(),
      body: JSON.stringify(this.user.privileges.map(pr => pr.id))
    });
  }
}
