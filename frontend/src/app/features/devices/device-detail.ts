import {
  Component,
  ElementRef,
  OnDestroy,
  afterNextRender,
  computed,
  effect,
  inject,
  input,
  signal,
  viewChild,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

import { ApiService } from '../../core/api.service';
import { DeviceStore } from '../../core/device-store';
import { ReadingView } from '../../core/models';
import { StatusChipComponent } from '../../shared/status-chip';
import { metric, relativeTime } from '../../shared/format';

Chart.register(...registerables);

@Component({
  selector: 'app-device-detail',
  imports: [MatCardModule, MatButtonModule, MatIconModule, RouterLink, StatusChipComponent],
  templateUrl: './device-detail.html',
  styleUrl: './device-detail.scss',
})
export class DeviceDetailComponent implements OnDestroy {
  readonly id = input.required<string>();

  private readonly store = inject(DeviceStore);
  private readonly api = inject(ApiService);

  // Optional: the canvas lives behind @if(device()), so it may not exist yet when readings arrive.
  private readonly canvas = viewChild<ElementRef<HTMLCanvasElement>>('chart');
  private chart?: Chart;

  private readonly readings = signal<ReadingView[]>([]);

  protected readonly device = computed(() => this.store.device(this.id())());
  protected readonly hasReadings = computed(() => this.readings().length > 0);

  constructor() {
    this.store.init();
    afterNextRender(() => this.loadReadings());
    // Draw once both the canvas is in the DOM and readings are loaded, in whichever order they arrive.
    effect(() => {
      const host = this.canvas();
      const data = this.readings();
      if (host && data.length) {
        this.draw(host.nativeElement, data);
      }
    });
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  private loadReadings(): void {
    this.api.getReadings(this.id(), { size: 100 }).subscribe((page) => {
      this.readings.set([...page.content].reverse());
    });
  }

  private draw(canvasEl: HTMLCanvasElement, readings: ReadingView[]): void {
    const labels = readings.map((r) =>
      new Date(r.recordedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    );
    const config: ChartConfiguration<'line'> = {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: 'Speed (km/h)',
            data: readings.map((r) => r.speedKmh),
            borderColor: '#1565c0',
            backgroundColor: 'rgba(21, 101, 192, 0.12)',
            tension: 0.3,
            yAxisID: 'y',
            pointRadius: 0,
          },
          {
            label: 'Battery (%)',
            data: readings.map((r) => r.batteryPct),
            borderColor: '#2e7d32',
            backgroundColor: 'rgba(46, 125, 50, 0.12)',
            tension: 0.3,
            yAxisID: 'y1',
            pointRadius: 0,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        scales: {
          y: { position: 'left', title: { display: true, text: 'km/h' } },
          y1: {
            position: 'right',
            min: 0,
            max: 100,
            grid: { drawOnChartArea: false },
            title: { display: true, text: '%' },
          },
        },
      },
    };
    this.chart?.destroy();
    this.chart = new Chart(canvasEl, config);
  }

  protected value(value: number | null | undefined, unit: string, digits = 0): string {
    return metric(value, unit, digits);
  }

  protected relative(iso: string | null): string {
    return relativeTime(iso);
  }
}
