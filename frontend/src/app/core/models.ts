export type DeviceType = 'CAR' | 'VAN' | 'TRUCK' | 'BIKE';
export type DeviceStatus = 'ONLINE' | 'OFFLINE' | 'UNKNOWN';
export type AlertType = 'THRESHOLD' | 'GEOFENCE' | 'OFFLINE';
export type AlertSeverity = 'INFO' | 'WARNING' | 'CRITICAL';
export type AlertStatus = 'ACTIVE' | 'ACKNOWLEDGED' | 'RESOLVED';
export type Metric = 'SPEED' | 'BATTERY' | 'FUEL' | 'ENGINE_TEMP';
export type ComparisonOperator = 'GT' | 'GTE' | 'LT' | 'LTE';
export type GeofenceMode = 'ENTER' | 'EXIT';

export interface DeviceView {
  deviceId: string;
  name: string;
  type: DeviceType;
  status: DeviceStatus;
  firstSeenAt: string | null;
  lastSeenAt: string | null;
  lastLatitude: number | null;
  lastLongitude: number | null;
  lastSpeedKmh: number | null;
  lastBatteryPct: number | null;
  lastFuelPct: number | null;
  lastEngineTempC: number | null;
  lastOdometerKm: number | null;
}

export interface ReadingView {
  id: number;
  deviceId: string;
  recordedAt: string;
  receivedAt: string;
  latitude: number;
  longitude: number;
  speedKmh: number;
  batteryPct: number | null;
  fuelPct: number | null;
  engineTempC: number | null;
  odometerKm: number | null;
}

export interface AlertView {
  id: number;
  deviceId: string;
  ruleId: number | null;
  type: AlertType;
  severity: AlertSeverity;
  status: AlertStatus;
  message: string;
  observedValue: number | null;
  threshold: number | null;
  createdAt: string;
  acknowledgedAt: string | null;
  resolvedAt: string | null;
}

export interface AlertRuleView {
  id: number;
  name: string;
  type: AlertType;
  metric: Metric | null;
  operator: ComparisonOperator | null;
  threshold: number | null;
  geofenceId: number | null;
  geofenceName: string | null;
  geofenceMode: GeofenceMode | null;
  deviceType: DeviceType | null;
  severity: AlertSeverity;
  enabled: boolean;
  createdAt: string;
}

export interface RuleRequest {
  name: string;
  type: AlertType;
  metric?: Metric | null;
  operator?: ComparisonOperator | null;
  threshold?: number | null;
  geofenceId?: number | null;
  geofenceMode?: GeofenceMode | null;
  deviceType?: DeviceType | null;
  severity: AlertSeverity;
  enabled?: boolean;
}

export interface GeofenceView {
  id: number;
  name: string;
  centerLat: number;
  centerLng: number;
  radiusM: number;
  createdAt: string;
}

export interface GeofenceRequest {
  name: string;
  centerLat: number;
  centerLng: number;
  radiusM: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  number: number;
  size: number;
  totalPages: number;
}

export interface ReadingQuery {
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export interface AlertQuery {
  status?: AlertStatus;
  deviceId?: string;
  page?: number;
  size?: number;
}
