import { Component } from '@angular/core';
import * as XLSX from 'xlsx';
import { TranslationService } from './i18n/translation.service';

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
        <button mat-raised-button color="accent" (click)="exportExcel()" [disabled]="!yearBreakdown.length">{{ 'EXPORT_EXCEL' | t }}</button>
      </mat-card-actions>
      <mat-card-content *ngIf="totalDays !== undefined">
        <p>{{ 'TOTAL_DAYS' | t }}: {{ totalDays }}</p>
        <table class="result-table">
          <thead>
            <tr>
              <th>{{ 'YEAR' | t }}</th>
              <th>{{ 'DAYS' | t }}</th>
              <th>{{ 'AMOUNT' | t }}</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let r of yearBreakdown">
              <td>{{ r.year }}</td>
              <td>{{ r.days }}</td>
              <td>{{ r.amount }}</td>
            </tr>
          </tbody>
        </table>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .cost-card { max-width: 600px; margin: 1rem auto; display: block; }
    .form-row { display: flex; flex-direction: column; gap: 0.5rem; }
    .result-table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
    .result-table th, .result-table td { border: 1px solid #ccc; padding: 0.25rem 0.5rem; }
  `]
})
export class CostSplitComponent {
  startDate = '20231201';
  endDate = '20240110';
  currentYearLastDay = '20231231';
  amount = '10000,50';

  totalDays?: number;
  yearBreakdown: { year: number; days: number; amount: string }[] = [];

  constructor(private ts: TranslationService) {}

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
      this.yearBreakdown = [];

      const dayMs = 24 * 60 * 60 * 1000;

      // first segment up to provided last day
      let segStart = start;
      let segEnd = last < end ? last : end;
      if (segEnd >= segStart) {
        const days = Math.floor((segEnd.getTime() - segStart.getTime()) / dayMs) + 1;
        this.yearBreakdown.push({
          year: segStart.getFullYear(),
          days,
          amount: ((amountNum / diff) * days).toFixed(2).replace('.', ',')
        });
      }

      segStart = new Date(segEnd.getTime() + dayMs);

      while (segStart <= end) {
        const year = segStart.getFullYear();
        segEnd = new Date(year, 11, 31);
        if (segEnd > end) segEnd = end;
        const days = Math.floor((segEnd.getTime() - segStart.getTime()) / dayMs) + 1;
        this.yearBreakdown.push({
          year,
          days,
          amount: ((amountNum / diff) * days).toFixed(2).replace('.', ',')
        });
        segStart = new Date(segEnd.getTime() + dayMs);
      }
    } catch (e: any) {
      if (e.message === 'bad date') {
        alert("Hibás dátumformátum. Kérlek 'ÉÉÉÉHHNN' formátumot használj.");
      } else if (e.message === 'amount') {
        alert('Hibás összeg formátum. Kérlek számot adj meg.');
      } else {
        alert('Ismeretlen hiba történt.');
      }
      this.totalDays = undefined;
      this.yearBreakdown = [];
    }
  }

  exportExcel() {
    if (!this.yearBreakdown.length) return;
    const data: any[][] = [];
    data.push([this.ts.translate('TOTAL_DAYS'), this.totalDays ?? 0]);
    data.push([this.ts.translate('YEAR'), this.ts.translate('DAYS'), this.ts.translate('AMOUNT')]);
    for (const r of this.yearBreakdown) {
      data.push([r.year, r.days, r.amount]);
    }
    const ws = XLSX.utils.aoa_to_sheet(data);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Result');
    XLSX.writeFile(wb, 'cost_split.xlsx');
  }
}
