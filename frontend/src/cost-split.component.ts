import { Component } from '@angular/core';

@Component({
  selector: 'app-cost-split',
  template: `
    <mat-card class="cost-card">
      <mat-card-title>{{ 'COST_SPLIT' | t }}</mat-card-title>
      <mat-card-content>
        <div class="form-row">
          <mat-form-field appearance="fill">
            <mat-label>{{ 'START_DATE' | t }}</mat-label>
            <input matInput [(ngModel)]="startDate" placeholder="20231201">
          </mat-form-field>
          <mat-form-field appearance="fill">
            <mat-label>{{ 'END_DATE' | t }}</mat-label>
            <input matInput [(ngModel)]="endDate" placeholder="20240110">
          </mat-form-field>
          <mat-form-field appearance="fill">
            <mat-label>{{ 'CURRENT_YEAR_LAST_DAY' | t }}</mat-label>
            <input matInput [(ngModel)]="currentYearLastDay" placeholder="20231231">
          </mat-form-field>
          <mat-form-field appearance="fill">
            <mat-label>{{ 'AMOUNT' | t }}</mat-label>
            <input matInput [(ngModel)]="amount" placeholder="10000,50">
          </mat-form-field>
        </div>
      </mat-card-content>
      <mat-card-actions>
        <button mat-raised-button color="primary" (click)="calculate()">{{ 'CALCULATE' | t }}</button>
      </mat-card-actions>
      <mat-card-content *ngIf="totalDays !== undefined">
        <p>{{ 'TOTAL_DAYS' | t }}: {{ totalDays }}</p>
        <p>{{ 'CURRENT_YEAR_DAYS' | t }}: {{ currentYearDays }}</p>
        <p>{{ 'NEXT_YEAR_DAYS' | t }}: {{ nextYearDays }}</p>
        <p>{{ 'CURRENT_YEAR_AMOUNT' | t }}: {{ currentYearAmount }}</p>
        <p>{{ 'NEXT_YEAR_AMOUNT' | t }}: {{ nextYearAmount }}</p>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .cost-card { max-width: 600px; margin: 1rem auto; display: block; }
    .form-row { display: flex; flex-direction: column; gap: 0.5rem; }
  `]
})
export class CostSplitComponent {
  startDate = '20231201';
  endDate = '20240110';
  currentYearLastDay = '20231231';
  amount = '10000,50';

  totalDays?: number;
  currentYearDays?: number;
  nextYearDays?: number;
  currentYearAmount?: string;
  nextYearAmount?: string;

  private parseDate(s: string): Date {
    if (!/^(\d{8})$/.test(s)) throw new Error('bad date');
    const y = Number(s.slice(0, 4));
    const m = Number(s.slice(4, 6)) - 1;
    const d = Number(s.slice(6, 8));
    return new Date(y, m, d);
  }

  calculate() {
    try {
      const start = this.parseDate(this.startDate);
      const end = this.parseDate(this.endDate);
      const last = this.parseDate(this.currentYearLastDay);

      const amountNum = parseFloat(this.amount.replace(',', '.'));
      if (isNaN(amountNum)) throw new Error('amount');

      if (start > end) {
        alert('Az időszak kezdő dátuma nem lehet később, mint a befejező dátuma.');
        return;
      }

      const diff = Math.floor((end.getTime() - start.getTime()) / 86400000) + 1;
      if (diff <= 0) {
        alert('Az összes napok száma nulla vagy negatív. Ellenőrizd a dátumokat.');
        return;
      }

      this.totalDays = diff;

      let current = Math.floor((last.getTime() - start.getTime()) / 86400000) + 1;
      current = Math.max(0, current);
      current = Math.min(current, diff);
      this.currentYearDays = current;

      const next = diff - current;
      this.nextYearDays = next;

      const currentAmount = (amountNum / diff) * current;
      const nextAmount = (amountNum / diff) * next;
      this.currentYearAmount = currentAmount.toFixed(2).replace('.', ',');
      this.nextYearAmount = nextAmount.toFixed(2).replace('.', ',');
    } catch (e: any) {
      if (e.message === 'bad date') {
        alert("Hibás dátumformátum. Kérlek 'ÉÉÉÉHHNN' formátumot használj.");
      } else if (e.message === 'amount') {
        alert('Hibás összeg formátum. Kérlek számot adj meg.');
      } else {
        alert('Ismeretlen hiba történt.');
      }
      this.totalDays = this.currentYearDays = this.nextYearDays = undefined;
      this.currentYearAmount = this.nextYearAmount = undefined;
    }
  }
}
