import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { DeviceStore } from './device-store';
import { DeviceView } from './models';

function device(overrides: Partial<DeviceView>): DeviceView {
  return {
    deviceId: 'veh-1',
    name: 'Truck 1',
    type: 'TRUCK',
    status: 'ONLINE',
    firstSeenAt: '2026-06-01T00:00:00Z',
    lastSeenAt: '2026-06-26T10:00:00Z',
    lastLatitude: 40.4,
    lastLongitude: -3.7,
    lastSpeedKmh: 50,
    lastBatteryPct: 80,
    lastFuelPct: 60,
    lastEngineTempC: 90,
    lastOdometerKm: 1200,
    ...overrides,
  };
}

describe('DeviceStore', () => {
  let store: DeviceStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DeviceStore,
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    store = TestBed.inject(DeviceStore);
  });

  it('inserts new devices and keeps them sorted by name', () => {
    store.upsert(device({ deviceId: 'b', name: 'Zephyr' }));
    store.upsert(device({ deviceId: 'a', name: 'Atlas' }));

    expect(store.devices().map((d) => d.name)).toEqual(['Atlas', 'Zephyr']);
    expect(store.total()).toBe(2);
  });

  it('replaces an existing device on upsert by id', () => {
    store.upsert(device({ deviceId: 'veh-1', status: 'ONLINE' }));
    store.upsert(device({ deviceId: 'veh-1', status: 'OFFLINE' }));

    expect(store.total()).toBe(1);
    expect(store.devices()[0].status).toBe('OFFLINE');
    expect(store.online()).toBe(0);
  });

  it('counts only online devices', () => {
    store.upsert(device({ deviceId: 'veh-1', status: 'ONLINE' }));
    store.upsert(device({ deviceId: 'veh-2', status: 'OFFLINE' }));
    store.upsert(device({ deviceId: 'veh-3', status: 'ONLINE' }));

    expect(store.online()).toBe(2);
  });
});
