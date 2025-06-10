import { Pipe, PipeTransform } from '@angular/core';
import { TranslationService } from './translation.service';

@Pipe({ name: 't', pure: false })
export class TranslatePipe implements PipeTransform {
  constructor(private ts: TranslationService) {}
  transform(key: string, params?: Record<string, string | number>): string {
    return this.ts.translate(key, params);
  }
}
