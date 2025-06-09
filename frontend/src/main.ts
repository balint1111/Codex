import { Component, NgModule } from '@angular/core';
import '@angular/compiler';
import 'zone.js';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from "@angular/forms";
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

@Component({
  selector: 'app-root',
  template: `
    <h1>Codex Dashboard</h1>
    <div *ngIf="user">
      Welcome {{user}} | <a (click)="logout()" href="#">Logout</a>
    </div>
    <div *ngIf="!user">
      <input [(ngModel)]="username" placeholder="username" />
      <input [(ngModel)]="password" placeholder="password" type="password" />
      <button (click)="login()">Login</button>
    </div>
  `
})
class AppComponent {
  user?: string;
  username = '';
  password = '';

  login() {
    // TODO call backend
    this.user = this.username;
  }

  logout() {
    this.user = undefined;
  }
}

@NgModule({
  declarations: [AppComponent],
  imports: [BrowserModule, FormsModule],
  bootstrap: [AppComponent]
})
class AppModule {}

platformBrowserDynamic().bootstrapModule(AppModule);
