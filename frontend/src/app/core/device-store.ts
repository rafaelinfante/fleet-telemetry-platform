import { Injectable, computed, inject, signal } from '@angular/core';

import { ApiService } from './api.service';
import { DeviceView } from './models';
import { StompService } from './stomp.service';

@Injectable({ providedIn: 'root' })
export class DeviceStore {
  private readonly api = inject(ApiService);
  private readonly stomp = inject(StompService);

  private readonly byId = signal<Map<string, DeviceView>>(new Map());
  private readonly loaded = signal(false);

  readonly devices = computed(() =>
    [...this.byId().values()].sort((a, b) => a.name.localeCompare(b.name)),
  );
  readonly online = computed(() => this.devices().filter((d) => d.status === 'ONLINE').length);
  readonly total = computed(() => this.byId().size);
  readonly isLoaded = this.loaded.asReadonly();

  constructor() {
    this.stomp.devices$.subscribe((device) => this.upsert(device));
  }

  init(): void {
    if (this.loaded()) {
      return;
    }
    this.api.listDevices().subscribe((devices) => {
      const map = new Map(devices.map((d) => [d.deviceId, d]));
      this.byId.set(map);
      this.loaded.set(true);
    });
    this.stomp.activate();
  }

  device(deviceId: string) {
    return computed(() => this.byId().get(deviceId) ?? null);
  }

  upsert(device: DeviceView): void {
    this.byId.update((current) => {
      const next = new Map(current);
      next.set(device.deviceId, device);
      return next;
    });
  }
}
