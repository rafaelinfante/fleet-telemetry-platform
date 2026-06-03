import { Component, computed, input } from '@angular/core';

import { AlertSeverity } from '../core/models';

@Component({
  selector: 'app-severity-chip',
  template: `<span class="severity-chip" [class]="cssClass()">{{ severity() }}</span>`,
  styles: `
    .severity-chip {
      display: inline-block;
      padding: 2px 10px;
      border-radius: 999px;
      font-size: 0.75rem;
    }
  `,
})
export class SeverityChipComponent {
  readonly severity = input.required<AlertSeverity>();
  protected readonly cssClass = computed(() => `severity-${this.severity().toLowerCase()}`);
}
