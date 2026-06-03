import { Component, computed, inject } from '@angular/core';
import {
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

import {
  AlertRuleView,
  AlertSeverity,
  AlertType,
  ComparisonOperator,
  DeviceType,
  GeofenceMode,
  GeofenceView,
  Metric,
  RuleRequest,
} from '../../core/models';

export interface RuleDialogData {
  rule: AlertRuleView | null;
  geofences: GeofenceView[];
}

@Component({
  selector: 'app-rule-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
  ],
  templateUrl: './rule-dialog.html',
  styleUrl: './rule-dialog.scss',
})
export class RuleDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<RuleDialogComponent, RuleRequest>);
  protected readonly data = inject<RuleDialogData>(MAT_DIALOG_DATA);

  protected readonly alertTypes: AlertType[] = ['THRESHOLD', 'GEOFENCE', 'OFFLINE'];
  protected readonly metrics: Metric[] = ['SPEED', 'BATTERY', 'FUEL', 'ENGINE_TEMP'];
  protected readonly operators: ComparisonOperator[] = ['GT', 'GTE', 'LT', 'LTE'];
  protected readonly geofenceModes: GeofenceMode[] = ['ENTER', 'EXIT'];
  protected readonly severities: AlertSeverity[] = ['INFO', 'WARNING', 'CRITICAL'];
  protected readonly deviceTypes: DeviceType[] = ['CAR', 'VAN', 'TRUCK', 'BIKE'];

  protected readonly form = this.fb.group({
    name: this.fb.nonNullable.control('', { validators: [Validators.required] }),
    type: this.fb.nonNullable.control<AlertType>('THRESHOLD', {
      validators: [Validators.required],
    }),
    metric: this.fb.control<Metric | null>(null),
    operator: this.fb.control<ComparisonOperator | null>(null),
    threshold: this.fb.control<number | null>(null),
    geofenceId: this.fb.control<number | null>(null),
    geofenceMode: this.fb.control<GeofenceMode | null>(null),
    deviceType: this.fb.control<DeviceType | null>(null),
    severity: this.fb.nonNullable.control<AlertSeverity>('WARNING', {
      validators: [Validators.required],
    }),
    enabled: this.fb.nonNullable.control(true),
  });

  private readonly type = toSignal(this.form.controls.type.valueChanges, {
    initialValue: this.form.controls.type.value,
  });
  protected readonly isThreshold = computed(() => this.type() === 'THRESHOLD');
  protected readonly isGeofence = computed(() => this.type() === 'GEOFENCE');

  protected readonly title = this.data.rule ? 'Edit rule' : 'New rule';

  constructor() {
    const rule = this.data.rule;
    if (rule) {
      this.form.patchValue({
        name: rule.name,
        type: rule.type,
        metric: rule.metric,
        operator: rule.operator,
        threshold: rule.threshold,
        geofenceId: rule.geofenceId,
        geofenceMode: rule.geofenceMode,
        deviceType: rule.deviceType,
        severity: rule.severity,
        enabled: rule.enabled,
      });
    }
    this.applyConditionalValidators(this.form.controls.type.value);
    this.form.controls.type.valueChanges.subscribe((value) =>
      this.applyConditionalValidators(value),
    );
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: RuleRequest = {
      name: value.name,
      type: value.type,
      severity: value.severity,
      enabled: value.enabled,
      metric: value.type === 'THRESHOLD' ? value.metric : null,
      operator: value.type === 'THRESHOLD' ? value.operator : null,
      threshold: value.type === 'THRESHOLD' ? value.threshold : null,
      geofenceId: value.type === 'GEOFENCE' ? value.geofenceId : null,
      geofenceMode: value.type === 'GEOFENCE' ? value.geofenceMode : null,
      deviceType: value.deviceType,
    };
    this.dialogRef.close(request);
  }

  protected cancel(): void {
    this.dialogRef.close();
  }

  private applyConditionalValidators(type: AlertType): void {
    const { metric, operator, threshold, geofenceId, geofenceMode } = this.form.controls;
    const required = type === 'THRESHOLD';
    const geofenceRequired = type === 'GEOFENCE';

    this.toggleRequired(metric, required);
    this.toggleRequired(operator, required);
    this.toggleRequired(threshold, required);
    this.toggleRequired(geofenceId, geofenceRequired);
    this.toggleRequired(geofenceMode, geofenceRequired);
  }

  private toggleRequired(control: FormControl, required: boolean): void {
    control.setValidators(required ? [Validators.required] : []);
    control.updateValueAndValidity({ emitEvent: false });
  }
}
