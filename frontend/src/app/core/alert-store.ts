import { Injectable, computed, inject, signal } from '@angular/core';

import { ApiService } from './api.service';
import { AlertView } from './models';
import { StompService } from './stomp.service';

const FEED_LIMIT = 200;

@Injectable({ providedIn: 'root' })
export class AlertStore {
  private readonly api = inject(ApiService);
  private readonly stomp = inject(StompService);

  private readonly items = signal<AlertView[]>([]);
  private readonly loaded = signal(false);

  readonly alerts = this.items.asReadonly();
  readonly isLoaded = this.loaded.asReadonly();
  readonly active = computed(() => this.items().filter((a) => a.status === 'ACTIVE'));
  readonly activeCritical = computed(() =>
    this.active().filter((a) => a.severity === 'CRITICAL'),
  );

  /** Device ids with at least one active critical alert — used to flag map markers red. */
  readonly criticalDeviceIds = computed(
    () => new Set(this.activeCritical().map((a) => a.deviceId)),
  );

  constructor() {
    this.stomp.alerts$.subscribe((alert) => this.merge(alert));
  }

  init(): void {
    if (this.loaded()) {
      return;
    }
    this.api.listAlerts({ size: FEED_LIMIT }).subscribe((page) => {
      this.items.set(page.content);
      this.loaded.set(true);
    });
    this.stomp.activate();
  }

  merge(alert: AlertView): void {
    this.items.update((current) => {
      const index = current.findIndex((a) => a.id === alert.id);
      if (index >= 0) {
        const next = [...current];
        next[index] = alert;
        return next;
      }
      return [alert, ...current].slice(0, FEED_LIMIT);
    });
  }

  replace(alert: AlertView): void {
    this.merge(alert);
  }
}
