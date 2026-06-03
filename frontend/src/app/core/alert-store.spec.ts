import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { AlertStore } from './alert-store';
import { AlertView } from './models';

function alert(overrides: Partial<AlertView>): AlertView {
  return {
    id: 1,
    deviceId: 'veh-1',
    ruleId: 10,
    type: 'THRESHOLD',
    severity: 'WARNING',
    status: 'ACTIVE',
    message: 'Speed over limit',
    observedValue: 120,
    threshold: 100,
    createdAt: '2026-06-26T10:00:00Z',
    acknowledgedAt: null,
    resolvedAt: null,
    ...overrides,
  };
}

describe('AlertStore', () => {
  let store: AlertStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AlertStore,
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    store = TestBed.inject(AlertStore);
  });

  it('prepends new alerts so the newest is first', () => {
    store.merge(alert({ id: 1 }));
    store.merge(alert({ id: 2, deviceId: 'veh-2' }));

    expect(store.alerts().map((a) => a.id)).toEqual([2, 1]);
  });

  it('updates an existing alert in place by id', () => {
    store.merge(alert({ id: 5, status: 'ACTIVE' }));
    store.merge(alert({ id: 5, status: 'RESOLVED' }));

    expect(store.alerts().length).toBe(1);
    expect(store.alerts()[0].status).toBe('RESOLVED');
  });

  it('exposes device ids that carry an active critical alert', () => {
    store.merge(alert({ id: 1, deviceId: 'veh-1', severity: 'CRITICAL', status: 'ACTIVE' }));
    store.merge(alert({ id: 2, deviceId: 'veh-2', severity: 'CRITICAL', status: 'RESOLVED' }));
    store.merge(alert({ id: 3, deviceId: 'veh-3', severity: 'WARNING', status: 'ACTIVE' }));

    const critical = store.criticalDeviceIds();
    expect(critical.has('veh-1')).toBe(true);
    expect(critical.has('veh-2')).toBe(false);
    expect(critical.has('veh-3')).toBe(false);
  });
});
