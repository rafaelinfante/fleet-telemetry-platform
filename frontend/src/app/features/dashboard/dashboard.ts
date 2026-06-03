import {
  Component,
  ElementRef,
  OnDestroy,
  afterNextRender,
  effect,
  inject,
  viewChild,
} from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import * as L from 'leaflet';

import { AlertStore } from '../../core/alert-store';
import { ApiService } from '../../core/api.service';
import { DeviceStore } from '../../core/device-store';
import { DeviceStatus, DeviceView } from '../../core/models';
import { AlertFeedComponent } from './alert-feed';

const STATUS_COLOURS: Record<DeviceStatus, string> = {
  ONLINE: '#2e7d32',
  OFFLINE: '#7a7a7a',
  UNKNOWN: '#c77700',
};
const CRITICAL_COLOUR = '#c62828';
const DEFAULT_CENTER: L.LatLngTuple = [40.4168, -3.7038];

@Component({
  selector: 'app-dashboard',
  imports: [MatCardModule, MatIconModule, AlertFeedComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class DashboardComponent implements OnDestroy {
  private readonly devices = inject(DeviceStore);
  private readonly alerts = inject(AlertStore);
  private readonly api = inject(ApiService);

  private readonly mapHost = viewChild.required<ElementRef<HTMLElement>>('map');

  private map?: L.Map;
  private readonly markers = new Map<string, L.CircleMarker>();
  private readonly geofenceLayer = L.layerGroup();
  private firstFit = true;

  protected readonly online = this.devices.online;
  protected readonly total = this.devices.total;
  protected readonly activeAlerts = this.alerts.active;

  constructor() {
    afterNextRender(() => {
      this.initMap();
      this.loadGeofences();
    });

    effect(() => {
      const list = this.devices.devices();
      const critical = this.alerts.criticalDeviceIds();
      if (this.map) {
        this.renderDevices(list, critical);
      }
    });
  }

  ngOnDestroy(): void {
    this.map?.remove();
    this.map = undefined;
    this.markers.clear();
  }

  private initMap(): void {
    const map = L.map(this.mapHost().nativeElement, {
      center: DEFAULT_CENTER,
      zoom: 12,
      preferCanvas: true,
    });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors',
    }).addTo(map);
    this.geofenceLayer.addTo(map);
    this.map = map;
    this.renderDevices(this.devices.devices(), this.alerts.criticalDeviceIds());
  }

  private loadGeofences(): void {
    this.api.listGeofences().subscribe((fences) => {
      this.geofenceLayer.clearLayers();
      for (const fence of fences) {
        L.circle([fence.centerLat, fence.centerLng], {
          radius: fence.radiusM,
          color: '#1565c0',
          weight: 1,
          fillColor: '#1565c0',
          fillOpacity: 0.06,
        })
          .bindTooltip(fence.name)
          .addTo(this.geofenceLayer);
      }
    });
  }

  private renderDevices(list: DeviceView[], critical: Set<string>): void {
    if (!this.map) {
      return;
    }
    const seen = new Set<string>();
    let positioned = false;

    for (const device of list) {
      // == null also catches undefined, which is what the backend's non_null inclusion produces.
      if (device.lastLatitude == null || device.lastLongitude == null) {
        continue;
      }
      seen.add(device.deviceId);
      positioned = true;
      const colour = critical.has(device.deviceId)
        ? CRITICAL_COLOUR
        : STATUS_COLOURS[device.status];
      const position: L.LatLngTuple = [device.lastLatitude, device.lastLongitude];

      const existing = this.markers.get(device.deviceId);
      if (existing) {
        existing.setLatLng(position);
        existing.setStyle({ color: colour, fillColor: colour });
        existing.setTooltipContent(this.popup(device));
      } else {
        const marker = L.circleMarker(position, {
          radius: 8,
          color: colour,
          fillColor: colour,
          fillOpacity: 0.85,
          weight: 2,
        })
          .bindPopup(this.popup(device))
          .addTo(this.map);
        this.markers.set(device.deviceId, marker);
      }
    }

    for (const [id, marker] of this.markers) {
      if (!seen.has(id)) {
        marker.remove();
        this.markers.delete(id);
      }
    }

    if (positioned && this.markers.size && this.firstFit) {
      const group = L.featureGroup([...this.markers.values()]);
      this.map.fitBounds(group.getBounds().pad(0.2));
      this.firstFit = false;
    }
  }

  private popup(device: DeviceView): string {
    const speed = device.lastSpeedKmh != null ? `${device.lastSpeedKmh.toFixed(0)} km/h` : '—';
    const battery = device.lastBatteryPct != null ? `${device.lastBatteryPct.toFixed(0)}%` : '—';
    return `<strong>${device.name}</strong><br>${device.deviceId}<br>Speed: ${speed}<br>Battery: ${battery}`;
  }
}
