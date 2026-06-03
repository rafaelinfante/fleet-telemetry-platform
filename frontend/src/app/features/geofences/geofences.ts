import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';

import { ApiService } from '../../core/api.service';
import { GeofenceRequest, GeofenceView } from '../../core/models';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog';
import { GeofenceDialogComponent } from './geofence-dialog';

@Component({
  selector: 'app-geofences',
  imports: [MatCardModule, MatTableModule, MatButtonModule, MatIconModule],
  templateUrl: './geofences.html',
  styleUrl: './geofences.scss',
})
export class GeofencesComponent {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly geofences = signal<GeofenceView[]>([]);
  protected readonly columns = ['name', 'center', 'radius', 'actions'];

  constructor() {
    this.reload();
  }

  protected create(): void {
    this.openDialog(null);
  }

  protected edit(fence: GeofenceView): void {
    this.openDialog(fence);
  }

  protected remove(fence: GeofenceView): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete geofence',
        message: `Delete "${fence.name}"? Rules referencing it will need updating.`,
      },
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) {
        return;
      }
      this.api.deleteGeofence(fence.id).subscribe(() => {
        this.snackBar.open('Geofence deleted', 'Dismiss', { duration: 3000 });
        this.reload();
      });
    });
  }

  protected center(fence: GeofenceView): string {
    return `${fence.centerLat.toFixed(4)}, ${fence.centerLng.toFixed(4)}`;
  }

  private openDialog(fence: GeofenceView | null): void {
    const ref = this.dialog.open(GeofenceDialogComponent, { data: fence, width: '420px' });
    ref.afterClosed().subscribe((request?: GeofenceRequest) => {
      if (!request) {
        return;
      }
      const call = fence
        ? this.api.updateGeofence(fence.id, request)
        : this.api.createGeofence(request);
      call.subscribe(() => {
        this.snackBar.open(fence ? 'Geofence updated' : 'Geofence created', 'Dismiss', {
          duration: 3000,
        });
        this.reload();
      });
    });
  }

  private reload(): void {
    this.api.listGeofences().subscribe((fences) => this.geofences.set(fences));
  }
}
