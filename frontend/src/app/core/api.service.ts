import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  AlertQuery,
  AlertRuleView,
  AlertView,
  DeviceView,
  GeofenceRequest,
  GeofenceView,
  Page,
  ReadingQuery,
  ReadingView,
  RuleRequest,
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  listDevices(): Observable<DeviceView[]> {
    return this.http.get<DeviceView[]>('/api/devices');
  }

  getDevice(deviceId: string): Observable<DeviceView> {
    return this.http.get<DeviceView>(`/api/devices/${encodeURIComponent(deviceId)}`);
  }

  getReadings(deviceId: string, query: ReadingQuery = {}): Observable<Page<ReadingView>> {
    return this.http.get<Page<ReadingView>>(
      `/api/devices/${encodeURIComponent(deviceId)}/readings`,
      { params: toParams(query) },
    );
  }

  listAlerts(query: AlertQuery = {}): Observable<Page<AlertView>> {
    return this.http.get<Page<AlertView>>('/api/alerts', { params: toParams(query) });
  }

  acknowledgeAlert(id: number): Observable<AlertView> {
    return this.http.post<AlertView>(`/api/alerts/${id}/acknowledge`, {});
  }

  resolveAlert(id: number): Observable<AlertView> {
    return this.http.post<AlertView>(`/api/alerts/${id}/resolve`, {});
  }

  listRules(): Observable<AlertRuleView[]> {
    return this.http.get<AlertRuleView[]>('/api/rules');
  }

  createRule(request: RuleRequest): Observable<AlertRuleView> {
    return this.http.post<AlertRuleView>('/api/rules', request);
  }

  updateRule(id: number, request: RuleRequest): Observable<AlertRuleView> {
    return this.http.put<AlertRuleView>(`/api/rules/${id}`, request);
  }

  deleteRule(id: number): Observable<void> {
    return this.http.delete<void>(`/api/rules/${id}`);
  }

  listGeofences(): Observable<GeofenceView[]> {
    return this.http.get<GeofenceView[]>('/api/geofences');
  }

  createGeofence(request: GeofenceRequest): Observable<GeofenceView> {
    return this.http.post<GeofenceView>('/api/geofences', request);
  }

  updateGeofence(id: number, request: GeofenceRequest): Observable<GeofenceView> {
    return this.http.put<GeofenceView>(`/api/geofences/${id}`, request);
  }

  deleteGeofence(id: number): Observable<void> {
    return this.http.delete<void>(`/api/geofences/${id}`);
  }
}

type QueryValue = string | number | undefined | null;

function toParams(query: object): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(query as Record<string, QueryValue>)) {
    if (value !== undefined && value !== null && value !== '') {
      params = params.set(key, String(value));
    }
  }
  return params;
}
