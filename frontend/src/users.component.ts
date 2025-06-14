import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { AuthService } from './auth.service';
import { UserService } from './services/user.service';
import { User } from './models/user';

@Component({
  selector: 'app-users',
  template: `
    <table mat-table [dataSource]="dataSource" class="mat-elevation-z8 clickable">
      <ng-container matColumnDef="id">
        <th mat-header-cell *matHeaderCellDef>{{ 'ID' | t }}</th>
        <td mat-cell *matCellDef="let u" (click)="editUser(u.id)">{{u.id}}</td>
      </ng-container>
      <ng-container matColumnDef="username">
        <th mat-header-cell *matHeaderCellDef>{{ 'NAME' | t }}</th>
        <td mat-cell *matCellDef="let u" (click)="editUser(u.id)">{{u.username}}</td>
      </ng-container>
      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>{{ 'ACTIONS' | t }}</th>
        <td mat-cell *matCellDef="let u">
          <button mat-icon-button color="warn" (click)="deleteUser(u.id)">
            <mat-icon>delete</mat-icon>
          </button>
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
      <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
    </table>
    <mat-paginator [pageSize]="10" [pageSizeOptions]="[5,10,20]"></mat-paginator>
  `,
  styles: [`
    table { width: 100%; }
    .clickable td { cursor: pointer; }
  `]
})
export class UsersComponent implements OnInit, AfterViewInit {
  displayedColumns = ['id', 'username', 'actions'];
  dataSource = new MatTableDataSource<User>([]);
  @ViewChild(MatPaginator) paginator?: MatPaginator;

  constructor(private auth: AuthService, private router: Router, private userService: UserService) {}

  ngOnInit() {
    this.loadUsers();
  }

  ngAfterViewInit() {
    if (this.paginator) {
      this.dataSource.paginator = this.paginator;
    }
  }

  loadUsers() {
    this.userService.list().subscribe(d => this.dataSource.data = d);
  }

  editUser(id: number) {
    this.router.navigate(['/users', id]);
  }

  deleteUser(id: number) {
    this.userService.delete(id).subscribe(() => this.loadUsers());
  }
}
