import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';

import { AppComponent } from './app.component';
import { NavbarComponent } from './navbar.component';
import { TopMenuComponent } from './top-menu.component';
import { DashboardComponent } from './dashboard.component';
import { UsersComponent } from './users.component';

const routes: Routes = [
  { path: 'dashboard', component: DashboardComponent },
  { path: 'users', component: UsersComponent },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
];

@NgModule({
  declarations: [AppComponent, NavbarComponent, TopMenuComponent, DashboardComponent, UsersComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    RouterModule.forRoot(routes),
    MatToolbarModule,
    MatButtonModule,
    MatSidenavModule,
    MatListModule,
    MatInputModule,
    MatFormFieldModule
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
