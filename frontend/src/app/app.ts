import { Component, inject } from '@angular/core';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AlertStore } from './core/alert-store';
import { DeviceStore } from './core/device-store';
import { StompService } from './core/stomp.service';

interface NavLink {
  path: string;
  label: string;
  icon: string;
  exact: boolean;
}

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatBadgeModule,
    MatTooltipModule,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly devices = inject(DeviceStore);
  private readonly alerts = inject(AlertStore);
  private readonly stomp = inject(StompService);

  protected readonly connected = this.stomp.connected;
  protected readonly onlineCount = this.devices.online;
  protected readonly deviceCount = this.devices.total;
  protected readonly criticalCount = this.alerts.activeCritical;

  protected readonly links: NavLink[] = [
    { path: '/', label: 'Dashboard', icon: 'dashboard', exact: true },
    { path: '/devices', label: 'Devices', icon: 'directions_car', exact: false },
    { path: '/alerts', label: 'Alerts', icon: 'notifications_active', exact: false },
    { path: '/rules', label: 'Rules', icon: 'rule', exact: false },
    { path: '/geofences', label: 'Geofences', icon: 'my_location', exact: false },
  ];

  constructor() {
    this.devices.init();
    this.alerts.init();
  }
}
