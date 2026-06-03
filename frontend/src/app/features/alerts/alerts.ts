import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';

import { AlertStore } from '../../core/alert-store';
import { ApiService } from '../../core/api.service';
import { AlertStatus, AlertView } from '../../core/models';
import { SeverityChipComponent } from '../../shared/severity-chip';
import { relativeTime } from '../../shared/format';

type StatusFilter = 'ALL' | AlertStatus;

@Component({
  selector: 'app-alerts',
  imports: [
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
    SeverityChipComponent,
  ],
  templateUrl: './alerts.html',
  styleUrl: './alerts.scss',
})
export class AlertsComponent {
  private readonly store = inject(AlertStore);
  private readonly api = inject(ApiService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly filter = signal<StatusFilter>('ALL');
  protected readonly columns = ['severity', 'status', 'device', 'message', 'created', 'actions'];

  protected readonly alerts = computed(() => {
    const status = this.filter();
    const all = this.store.alerts();
    return status === 'ALL' ? all : all.filter((a) => a.status === status);
  });

  protected setFilter(value: StatusFilter): void {
    this.filter.set(value);
  }

  protected acknowledge(alert: AlertView): void {
    this.api.acknowledgeAlert(alert.id).subscribe((updated) => {
      this.store.replace(updated);
      this.snackBar.open('Alert acknowledged', 'Dismiss', { duration: 3000 });
    });
  }

  protected resolve(alert: AlertView): void {
    this.api.resolveAlert(alert.id).subscribe((updated) => {
      this.store.replace(updated);
      this.snackBar.open('Alert resolved', 'Dismiss', { duration: 3000 });
    });
  }

  protected created(iso: string): string {
    return relativeTime(iso);
  }
}
