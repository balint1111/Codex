import { Component } from '@angular/core';
import { AuthService } from './auth.service';

interface VatResult { valid: boolean; name?: string; address?: string; }

@Component({
  selector: 'app-vat-check',
  template: `
    <mat-card class="vat-card">
      <mat-card-title>{{ 'VAT_CHECK' | t }}</mat-card-title>
      <mat-card-content>
        <mat-form-field appearance="fill">
          <mat-label>{{ 'COUNTRY_CODE' | t }}</mat-label>
          <input matInput [(ngModel)]="countryCode" placeholder="HU">
        </mat-form-field>
        <mat-form-field appearance="fill">
          <mat-label>{{ 'VAT_NUMBER' | t }}</mat-label>
          <input matInput [(ngModel)]="vatNumber" placeholder="12345676">
        </mat-form-field>
        <div *ngIf="result">
          <p>{{ 'VALID' | t }}: {{ result.valid }}</p>
          <p *ngIf="result.name">{{ 'NAME' | t }}: {{ result.name }}</p>
          <p *ngIf="result.address">{{ 'ADDRESS' | t }}: {{ result.address }}</p>
        </div>
      </mat-card-content>
      <mat-card-actions>
        <button mat-raised-button color="primary" (click)="check()">{{ 'CHECK' | t }}</button>
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    .vat-card { max-width: 400px; margin: 1rem auto; display: block; }
  `]
})
export class VatCheckComponent {
  countryCode = 'HU';
  vatNumber = '';
  result?: VatResult;

  constructor(private auth: AuthService) {}

  private authHeaders() { return this.auth.authHeaders(); }

  check() {
    fetch(`${this.auth.API_URL}/api/vat/check?countryCode=${this.countryCode}&vatNumber=${this.vatNumber}`, {
      headers: this.authHeaders()
    })
      .then(r => r.ok ? r.json() : Promise.reject())
      .then((d: VatResult) => this.result = d)
      .catch(() => alert('Error'));
  }
}
