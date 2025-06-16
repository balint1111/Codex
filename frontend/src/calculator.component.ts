import { Component, HostListener } from '@angular/core';

@Component({
  selector: 'app-calculator',
  template: `
    <mat-card class="calc-card">
      <div class="display">{{ display }}</div>
    </mat-card>
  `,
  styles: [`
    .calc-card { margin: 1rem; }
    .display { font-family: monospace; text-align: right; padding: .5rem; }
  `]
})
export class CalculatorComponent {
  display = '0';
  private expr = '';

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
    } else if (code === 'Escape') {
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
}
