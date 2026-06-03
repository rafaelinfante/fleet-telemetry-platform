import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { ApiService } from './api.service';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ApiService,
        provideZonelessChangeDetection(),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('requests devices from the relative collection endpoint', () => {
    service.listDevices().subscribe();
    const req = httpMock.expectOne('/api/devices');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('encodes the device id and forwards reading query params', () => {
    service.getReadings('veh/01', { size: 100, page: 2 }).subscribe();
    const req = httpMock.expectOne(
      (r) => r.url === '/api/devices/veh%2F01/readings',
    );
    expect(req.request.params.get('size')).toBe('100');
    expect(req.request.params.get('page')).toBe('2');
    req.flush({ content: [], totalElements: 0, number: 2, size: 100, totalPages: 0 });
  });

  it('omits empty alert query params', () => {
    service.listAlerts({ status: 'ACTIVE', deviceId: '' }).subscribe();
    const req = httpMock.expectOne((r) => r.url === '/api/alerts');
    expect(req.request.params.get('status')).toBe('ACTIVE');
    expect(req.request.params.has('deviceId')).toBe(false);
    req.flush({ content: [], totalElements: 0, number: 0, size: 50, totalPages: 0 });
  });

  it('posts an empty body when acknowledging an alert', () => {
    service.acknowledgeAlert(42).subscribe();
    const req = httpMock.expectOne('/api/alerts/42/acknowledge');
    expect(req.request.method).toBe('POST');
    req.flush({});
  });
});
