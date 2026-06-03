import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/dashboard/dashboard').then((m) => m.DashboardComponent),
    title: 'Live dashboard',
  },
  {
    path: 'devices',
    loadComponent: () => import('./features/devices/devices').then((m) => m.DevicesComponent),
    title: 'Devices',
  },
  {
    path: 'devices/:id',
    loadComponent: () =>
      import('./features/devices/device-detail').then((m) => m.DeviceDetailComponent),
    title: 'Device detail',
  },
  {
    path: 'alerts',
    loadComponent: () => import('./features/alerts/alerts').then((m) => m.AlertsComponent),
    title: 'Alerts',
  },
  {
    path: 'rules',
    loadComponent: () => import('./features/rules/rules').then((m) => m.RulesComponent),
    title: 'Alert rules',
  },
  {
    path: 'geofences',
    loadComponent: () =>
      import('./features/geofences/geofences').then((m) => m.GeofencesComponent),
    title: 'Geofences',
  },
  { path: '**', redirectTo: '' },
];
