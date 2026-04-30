import { Component, Inject, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import Chart from 'chart.js/auto';

@Component({
  selector: 'app-trend-chart-modal',
  standalone: true,
  imports: [CommonModule, MatDialogModule],
  template: `
    <div class="modal-container">
      <div class="modal-header">
        <h3>{{ data.sensorType }} - Full Trend Analysis</h3>
        <button class="close-btn" (click)="close()">×</button>
      </div>
      <div class="modal-body">
        <canvas #modalChart></canvas>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; width: 100%; height: 100%; }
    .modal-container { height: 100%; display: flex; flex-direction: column; background: #1a1d21; color: white; z-index: 999999}
    .modal-header { display: flex; justify-content: space-between; align-items: center; padding: 15px 25px; border-bottom: 1px solid #333; z-index: 999999}
    .modal-body { flex-grow: 1; padding: 20px; position: relative; min-height: 0; z-index: 999999}
    .close-btn { background: none; border: none; color: white; font-size: 28px; cursor: pointer; line-height: 1;z-index: 999999 }
    h3 { margin: 0; font-size: 1.2rem; color: #0dcaf0; }
  `]
})
export class TrendChartModalComponent implements AfterViewInit {
  @ViewChild('modalChart') modalChartCanvas!: ElementRef<HTMLCanvasElement>;
  private chart: Chart | undefined;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogRef: MatDialogRef<TrendChartModalComponent>
  ) {}

  ngAfterViewInit(): void {
    this.renderChart();
  }

  close(): void {
    this.dialogRef.close();
  }

  private renderChart(): void {
    const ctx = this.modalChartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    this.chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: this.data.labels,
        datasets: this.data.datasets.map((ds: any) => ({
          ...ds,
          pointRadius: ds.label === 'Previsione' ? 6 : 4
        }))
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: {
          legend: { display: true, labels: { color: '#8a8d98', font: { size: 14 } } }
        },
        scales: {
          x: { ticks: { color: '#8a8d98', font: { size: 12 } }, grid: { display: false } },
          y: {
            ticks: { color: '#8a8d98', font: { size: 12 } },
            grid: { color: 'rgba(255,255,255,0.05)' },
            grace: '10%'
          }
        }
      }
    });
  }
}
