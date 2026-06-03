import { Component, computed, input } from '@angular/core';

import { DeviceStatus } from '../core/models';

@Component({
  selector: 'app-status-chip',
  template: `<span class="status-chip" [class]="cssClass()">{{ status() }}</span>`,
  styles: `
    .status-chip {
      display: inline-block;
      padding: 2px 10px;
      border-radius: 999px;
      font-size: 0.75rem;
    }
  `,
})
export class StatusChipComponent {
  readonly status = input.required<DeviceStatus>();
  protected readonly cssClass = computed(() => `status-${this.status().toLowerCase()}`);
}
