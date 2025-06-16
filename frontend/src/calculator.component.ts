import { Component, HostListener } from '@angular/core';

@Component({
  selector: 'app-calculator',
  template: `
    <mat-card class="calc-card">
      <div class="display">{{ display }}</div>
      <div class="keyboard" *ngIf="showKeyboard">
        <button mat-button (click)="append('7')">7</button>
        <button mat-button (click)="append('8')">8</button>
        <button mat-button (click)="append('9')">9</button>
        <button mat-button (click)="append('/')">/</button>

        <button mat-button (click)="append('4')">4</button>
        <button mat-button (click)="append('5')">5</button>
        <button mat-button (click)="append('6')">6</button>
        <button mat-button (click)="append('*')">*</button>

        <button mat-button (click)="append('1')">1</button>
        <button mat-button (click)="append('2')">2</button>
        <button mat-button (click)="append('3')">3</button>
        <button mat-button (click)="append('-')">-</button>

        <button mat-button (click)="append('0')">0</button>
        <button mat-button (click)="append('.')">.</button>
        <button mat-button color="primary" (click)="evaluate()">=</button>
        <button mat-button (click)="append('+')">+</button>

        <button mat-button color="warn" class="wide" (click)="clear()">C</button>
        <button mat-button class="wide" (click)="copy()">Copy</button>
      </div>
      <button mat-button class="toggle" (click)="toggleKeyboard()">
        {{ showKeyboard ? 'Hide' : 'Show' }} Keys
      </button>
    </mat-card>
  `,
  styles: [`
    .calc-card { margin: 1rem; }
    .display { font-family: monospace; text-align: right; padding: .5rem; }
    .keyboard { display: grid; grid-template-columns: repeat(4, 1fr); gap: .25rem; margin-top: .5rem; }
    .wide { grid-column: span 2; }
    .toggle { margin-top: .5rem; }
  `]
})
export class CalculatorComponent {
  display = '0';
  private expr = '';
  showKeyboard = false;

  @HostListener('window:keydown', ['$event'])
  handleKey(event: KeyboardEvent) {
    const target = event.target as HTMLElement;
    if (target && ['INPUT', 'TEXTAREA'].includes(target.tagName)) return;
    const code = event.code;
    if (/^Numpad[0-9]$/.test(code)) {
      this.append(code.slice(-1));
    } else if (code === 'NumpadDecimal') {
      this.append('.');
    } else if (code === 'NumpadAdd') {
      this.append('+');
    } else if (code === 'NumpadSubtract') {
      this.append('-');
    } else if (code === 'NumpadMultiply') {
      this.append('*');
    } else if (code === 'NumpadDivide') {
      this.append('/');
    } else if (code === 'NumpadEnter' || code === 'Enter') {
      this.evaluate();
    } else if (code === 'Escape' || code === 'Delete') {
      this.clear();
    }
  }

  private append(ch: string) {
    this.expr += ch;
    this.display = this.expr;
  }

  private evaluate() {
    try {
      // eslint-disable-next-line no-eval
      const result = eval(this.expr);
      this.display = String(result);
      this.expr = this.display;
    } catch {
      this.display = 'Err';
      this.expr = '';
    }
  }

  private clear() {
    this.expr = '';
    this.display = '0';
  }

  toggleKeyboard() {
    this.showKeyboard = !this.showKeyboard;
  }

  copy() {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(this.display).catch(() => {});
    }
  }
}
