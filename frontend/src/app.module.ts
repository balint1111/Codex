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
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatCardModule } from '@angular/material/card';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSelectModule } from '@angular/material/select';

import { TranslatePipe } from './i18n/translate.pipe';

import { AppComponent } from './app.component';
import { NavbarComponent } from './navbar.component';
import { TopMenuComponent } from './top-menu.component';
import { DashboardComponent } from './dashboard.component';
import { UsersComponent } from './users.component';
import { UserEditComponent } from './user-edit.component';
import { LoginComponent } from './login.component';
import { RegisterComponent } from './register.component';
import { CostSplitComponent } from './cost-split.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'users', component: UsersComponent },
  { path: 'users/:id', component: UserEditComponent },
  { path: 'cost-split', component: CostSplitComponent },
  { path: '', redirectTo: 'login', pathMatch: 'full' }
];

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    TopMenuComponent,
    DashboardComponent,
    UsersComponent,
    UserEditComponent,
    LoginComponent,
    RegisterComponent,
    CostSplitComponent,
    TranslatePipe
  ],
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
    MatFormFieldModule,
    MatTableModule,
    MatPaginatorModule,
    MatIconModule,
    MatCheckboxModule,
    MatCardModule,
    MatSlideToggleModule,
    MatSelectModule
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
