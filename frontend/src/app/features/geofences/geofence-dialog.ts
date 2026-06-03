import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { GeofenceRequest, GeofenceView } from '../../core/models';

@Component({
  selector: 'app-geofence-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './geofence-dialog.html',
  styleUrl: './geofence-dialog.scss',
})
export class GeofenceDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<GeofenceDialogComponent, GeofenceRequest>);
  private readonly data = inject<GeofenceView | null>(MAT_DIALOG_DATA);

  protected readonly title = this.data ? 'Edit geofence' : 'New geofence';

  protected readonly form = this.fb.group({
    name: this.fb.nonNullable.control(this.data?.name ?? '', {
      validators: [Validators.required],
    }),
    centerLat: this.fb.control<number | null>(this.data?.centerLat ?? null, {
      validators: [Validators.required, Validators.min(-90), Validators.max(90)],
    }),
    centerLng: this.fb.control<number | null>(this.data?.centerLng ?? null, {
      validators: [Validators.required, Validators.min(-180), Validators.max(180)],
    }),
    radiusM: this.fb.control<number | null>(this.data?.radiusM ?? null, {
      validators: [Validators.required, Validators.min(1)],
    }),
  });

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.dialogRef.close({
      name: value.name,
      centerLat: value.centerLat!,
      centerLng: value.centerLng!,
      radiusM: value.radiusM!,
    });
  }

  protected cancel(): void {
    this.dialogRef.close();
  }
}
