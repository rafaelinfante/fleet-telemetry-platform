import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { forkJoin } from 'rxjs';

import { ApiService } from '../../core/api.service';
import { AlertRuleView, GeofenceView, RuleRequest } from '../../core/models';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog';
import { SeverityChipComponent } from '../../shared/severity-chip';
import { RuleDialogComponent, RuleDialogData } from './rule-dialog';

@Component({
  selector: 'app-rules',
  imports: [
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    SeverityChipComponent,
  ],
  templateUrl: './rules.html',
  styleUrl: './rules.scss',
})
export class RulesComponent {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly rules = signal<AlertRuleView[]>([]);
  private readonly geofences = signal<GeofenceView[]>([]);
  protected readonly columns = ['name', 'type', 'condition', 'severity', 'enabled', 'actions'];

  constructor() {
    this.reload();
  }

  protected create(): void {
    this.openDialog(null);
  }

  protected edit(rule: AlertRuleView): void {
    this.openDialog(rule);
  }

  protected remove(rule: AlertRuleView): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete rule',
        message: `Delete "${rule.name}"? Existing alerts are kept.`,
      },
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) {
        return;
      }
      this.api.deleteRule(rule.id).subscribe(() => {
        this.snackBar.open('Rule deleted', 'Dismiss', { duration: 3000 });
        this.reload();
      });
    });
  }

  protected condition(rule: AlertRuleView): string {
    if (rule.type === 'THRESHOLD') {
      return `${rule.metric} ${rule.operator} ${rule.threshold}`;
    }
    if (rule.type === 'GEOFENCE') {
      return `${rule.geofenceName ?? 'geofence'} · ${rule.geofenceMode}`;
    }
    return 'Device went offline';
  }

  private openDialog(rule: AlertRuleView | null): void {
    const data: RuleDialogData = { rule, geofences: this.geofences() };
    const ref = this.dialog.open(RuleDialogComponent, { data, width: '440px' });
    ref.afterClosed().subscribe((request?: RuleRequest) => {
      if (!request) {
        return;
      }
      const call = rule ? this.api.updateRule(rule.id, request) : this.api.createRule(request);
      call.subscribe(() => {
        this.snackBar.open(rule ? 'Rule updated' : 'Rule created', 'Dismiss', { duration: 3000 });
        this.reload();
      });
    });
  }

  private reload(): void {
    forkJoin({ rules: this.api.listRules(), geofences: this.api.listGeofences() }).subscribe(
      ({ rules, geofences }) => {
        this.rules.set(rules);
        this.geofences.set(geofences);
      },
    );
  }
}
