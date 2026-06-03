import { Component, inject, input } from '@angular/core';

import { AlertStore } from '../../core/alert-store';
import { SeverityChipComponent } from '../../shared/severity-chip';
import { relativeTime } from '../../shared/format';

@Component({
  selector: 'app-alert-feed',
  imports: [SeverityChipComponent],
  template: `
    <div class="feed">
      @for (alert of feed(); track alert.id) {
        <div
          class="feed-item"
          [class.fresh-critical]="alert.status === 'ACTIVE' && alert.severity === 'CRITICAL'"
        >
          <div class="feed-head">
            <app-severity-chip [severity]="alert.severity" />
            <span class="device">{{ alert.deviceId }}</span>
            <span class="when text-muted-soft">{{ relative(alert.createdAt) }}</span>
          </div>
          <div class="message">{{ alert.message }}</div>
        </div>
      } @empty {
        <p class="empty text-muted-soft">No alerts yet.</p>
      }
    </div>
  `,
  styles: `
    .feed {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      overflow-y: auto;
    }
    .feed-item {
      padding: 0.6rem 0.75rem;
      border-radius: 8px;
      background: var(--mat-sys-surface-container);
      border-left: 3px solid transparent;
    }
    .feed-item.fresh-critical {
      border-left-color: var(--severity-critical);
      background: color-mix(in srgb, var(--severity-critical) 8%, var(--mat-sys-surface-container));
    }
    .feed-head {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 0.25rem;
    }
    .device {
      font-weight: 500;
      font-size: 0.85rem;
    }
    .when {
      margin-left: auto;
      font-size: 0.75rem;
    }
    .message {
      font-size: 0.85rem;
    }
    .empty {
      padding: 1rem 0.75rem;
    }
  `,
})
export class AlertFeedComponent {
  private readonly store = inject(AlertStore);

  readonly limit = input(50);

  protected feed() {
    return this.store.alerts().slice(0, this.limit());
  }

  protected relative(iso: string): string {
    return relativeTime(iso);
  }
}
