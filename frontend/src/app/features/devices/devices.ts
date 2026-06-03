import { Component, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { Router } from '@angular/router';

import { DeviceStore } from '../../core/device-store';
import { DeviceView } from '../../core/models';
import { StatusChipComponent } from '../../shared/status-chip';
import { metric, relativeTime } from '../../shared/format';

@Component({
  selector: 'app-devices',
  imports: [MatCardModule, MatTableModule, StatusChipComponent],
  templateUrl: './devices.html',
  styleUrl: './devices.scss',
})
export class DevicesComponent {
  private readonly store = inject(DeviceStore);
  private readonly router = inject(Router);

  protected readonly devices = this.store.devices;
  protected readonly columns = ['deviceId', 'name', 'type', 'status', 'speed', 'battery', 'lastSeen'];

  protected open(device: DeviceView): void {
    this.router.navigate(['/devices', device.deviceId]);
  }

  protected speed(device: DeviceView): string {
    return metric(device.lastSpeedKmh, ' km/h');
  }

  protected battery(device: DeviceView): string {
    return metric(device.lastBatteryPct, '%');
  }

  protected lastSeen(device: DeviceView): string {
    return relativeTime(device.lastSeenAt);
  }
}
