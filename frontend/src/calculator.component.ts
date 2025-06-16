import { Component, HostListener } from '@angular/core';

@Component({
  selector: 'app-calculator',
  template: `
    <mat-card class="calc-card">
      <div class="header">
        <button
          mat-icon-button
          class="toggle"
          (click)="toggleKeyboard()"
          [attr.aria-label]="showKeyboard ? ('HIDE_KEYS' | t) : ('SHOW_KEYS' | t)"
        >
          <mat-icon>{{ showKeyboard ? 'expand_less' : 'expand_more' }}</mat-icon>
        </button>
        <div class="display">{{ display }}</div>
      </div>
      <div class="keyboard" *ngIf="showKeyboard">
        <div class="keys">
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
        </div>
        <div class="actions">
          <button mat-button color="warn" (click)="clear()">{{ 'CLEAR' | t }}</button>
          <button mat-button (click)="copy()">{{ 'COPY' | t }}</button>
        </div>
      </div>
    </mat-card>
  `,
  styles: [`
    .calc-card { margin: 1rem; }
    .header { display: flex; align-items: center; }
    .display { flex: 1; font-family: monospace; text-align: right; padding: .5rem; }
    .keyboard { margin-top: .5rem; }
    .keys {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: .25rem;
    }
    .keys button,
    .actions button {
      border: 1px solid rgba(0, 0, 0, .12);
    }
    .actions {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: .25rem;
      margin-top: .5rem;
      border-top: 1px solid rgba(0,0,0,.12);
      padding-top: .5rem;
    }
    .toggle { margin-right: .25rem; }
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
