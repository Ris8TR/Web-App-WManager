import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule, DecimalPipe, LocationStrategy, PathLocationStrategy, APP_BASE_HREF } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { Subscription } from "rxjs";
import * as L from 'leaflet';
import Chart from 'chart.js/auto';
import { parse } from 'terraformer-wkt-parser';
import { MatSnackBar } from "@angular/material/snack-bar";

import { ToolbarComponent } from "../../../elements/toolbar/toolbar.component";
import { SensorService } from "../../../../service/sensor.service";
import { SensorDataService } from "../../../../service/sensorData.service";
import { InterestAreaService } from "../../../../service/interestArea.service";
import { AnalyticService } from "../../../../service/analytic.service";
import { CookieService } from "ngx-cookie-service";

import { SensorDto } from "../../../../model/sensorDto";
import { InterestArea } from "../../../../model/interestArea";
import { SensorData } from "../../../../model/sensorData";
import { DateDto } from "../../../../model/dateDto";
import {TrendChartModalComponent} from "../../../elements/trend-chart-modal/trend-chart-modal.component";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-interest-area-viewer',
  standalone: true,
  imports: [CommonModule, FormsModule, ToolbarComponent, DecimalPipe],
  providers: [
    { provide: LocationStrategy, useClass: PathLocationStrategy },
    { provide: APP_BASE_HREF, useValue: '/' }
  ],
  templateUrl: './interest-area-viewer.component.html',
  styleUrl: './interest-area-viewer.component.css'
})
export class InterestAreaViewerComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('forecastInterval') forecastIntervalElement!: ElementRef<HTMLSelectElement>;
  @ViewChild('trendChart') trendChartCanvas!: ElementRef<HTMLCanvasElement>;

  // --- Map & Layers ---
  private map: L.Map | undefined;
  private layerGroup: L.LayerGroup | undefined;
  private drawnLayers: L.Layer[] = [];
  private subscription: Subscription = new Subscription();

  // --- State Management ---
  id: string | null | undefined;
  interestArea: InterestArea | undefined;
  selectedSensor: string | undefined;
  public sensors: SensorDto[] = [];
  public selectedSensorType: string = "CO2";
  public sensorTypeList: string[] = [];
  public logStringResult: string = 'Login';
  isRealTime = false;
  isPanelVisible = true;

  // --- Analytics State ---
  public showTrend = false;
  public showAnomalies = false;
  public predictionValue: number | null = null;
  protected isForecast = false;
  protected isObservation = false;
  private trendChart: Chart | undefined;

  // --- Data Storage ---
  private sensorDataLocalList: SensorData[] = [];
  private sensorTrendLocalList: SensorData[] = [];
  private cachedData = new Map<string, [number, number, number][]>();

  // --- UI Configuration ---
  public selectedInterval: string = '5';
  public startDate?: string;
  public endDate?: string;
  public startHour?: string;
  public endHour?: string;

  temperatureScale = [
    { label: '-10', color: '#0030ff' }, { label: '0', color: '#00ebbd' },
    { label: '20', color: '#ffed00' }, { label: '40', color: '#940056' }
  ];

  constructor(
    private sensorDataService: SensorDataService,
    private interestAreaService: InterestAreaService,
    private sensorService: SensorService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private cookieService: CookieService,
    public toolbarComponent: ToolbarComponent,
    private analyticsService: AnalyticService
  ) {}

  // ========================================================================
  //  LIFECYCLE
  // ========================================================================

  ngOnInit(): void {
    this.subscription = this.interestAreaService.currentId$.subscribe(id => {
      this.id = id;
      this.reloadComponentData();
    });

    this.subscription.add(this.toolbarComponent.isForecast$.subscribe(v => this.isForecast = v));
    this.subscription.add(this.toolbarComponent.isObservation$.subscribe(v => this.isObservation = v));

    if (!this.id) this.snackBar.open("Selezionare un'area di interesse", "ok");

    this.interestAreaService.getInterestArea(this.id!).subscribe(area => {
      this.interestArea = area;
      this.toolbarComponent.inArea = true;
      this.toolbarComponent.isObservation = true;
      this.toolbarComponent.areaName = area.name;
      if (this.interestArea?.geometry) this.drawInterestArea(this.interestArea.geometry);
    });
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      if (!this.map) this.initializeMap();
      if (!this.layerGroup) this.layerGroup = L.layerGroup().addTo(this.map!);
      this.reloadComponentData();
      this.logStringResult = this.toolbarComponent.logStringResult;
    }, 10);
  }

  ngOnDestroy(): void {
    if (this.map) this.map.remove();
    this.subscription.unsubscribe();
    this.toolbarComponent.inArea = false;
  }

  // ========================================================================
  //  DATA MANAGEMENT
  // ========================================================================

  private reloadComponentData(): void {
    if (!this.id) return;
    this.cachedData.clear();
    this.interestAreaService.getInterestArea(this.id!).subscribe(area => this.interestArea = area);
    this.loadSensors();
    this.loadAllSensorData();
    if (this.interestArea?.geometry) this.drawInterestArea(this.interestArea.geometry);
  }

  private loadSensors(): void {
    this.sensorService.findByInterestAreaId(this.id!).subscribe(sensors => {
      this.sensors = sensors;
      if (sensors.length > 0) this.selectedSensor = sensors[0].id;
    });
  }

  private loadAllSensorData(): void {
    if (!this.map || !this.id) return;
    this.sensorDataService.getLastPrivateSensorDataByInterestAreaId(this.id!).subscribe({
      next: (res: any) => {
        if (res?.sensorData) {
          this.sensorDataLocalList = res.sensorData;
          this.sensorTypeList = res.sensorAreaTypes;
          this.updateGrid();
        }
      },
      error: (err) => console.error("Errore caricamento dati:", err)
    });
  }

  // ========================================================================
  //  MAP RENDERING LOGIC
  // ========================================================================

  private updateGrid(): void {
    if (!this.map || !this.selectedSensor) return;
    if (this.layerGroup) this.layerGroup.clearLayers();

    const token = this.cookieService.get('token');
    const authHeader = `Bearer ${token}`;
    const sensorId = this.selectedSensor;
    const key = this.selectedSensorType;

    // 1. Draw Base Layer (Trend or Standard)
    if (this.showTrend) {
      this.analyticsService.getSensorTrend(sensorId, authHeader, key).subscribe({
        next: (dataList: any[]) => {
          this.sensorTrendLocalList = dataList;
          const heatData = this.sensorDataLocalList
            .filter(d => d.latitude && d.longitude)
            .map((d): [number, number, number] => [ // <--- Aggiungi il tipo di ritorno qui
              d.latitude!,
              d.longitude!,
              (d.payload as any)?.[this.selectedSensorType] ?? 0
            ]);

          this.addPointsToMap(heatData, true);
          this.updateTrendChart();
          if (this.showAnomalies) this.renderAnomaliesOverlay(authHeader);
        },
        error: (err) => console.error("Errore trend:", err)
      });
    } else {
      this.sensorTrendLocalList = [];
      this.processAndMapLocalData();
      if (this.showAnomalies) this.renderAnomaliesOverlay(authHeader);
    }

    // 2. Always update chart if sensor is active
    this.updateTrendChart();
  }

  private processAndMapLocalData(): void {
    const heatData = this.sensorDataLocalList
      .filter(d => d.latitude && d.longitude)
      .map((d): [number, number, number] => [ // <--- Aggiungi il tipo di ritorno qui
        d.latitude!,
        d.longitude!,
        (d.payload as any)?.[this.selectedSensorType] ?? 0
      ]);

    this.addPointsToMap(heatData, false);
  }

  private addPointsToMap(heatData: [number, number, number][], isTrend: boolean): void {
    heatData.forEach(([lat, lng, value]) => {
      const marker = L.circleMarker([lat, lng], {
        radius: isTrend ? 10 : 6,
        fillColor: isTrend ? '#0dcaf0' : this.getColor(value),
        color: isTrend ? '#ffffff' : '#000',
        weight: 1,
        opacity: 1,
        fillOpacity: 0.7
      }).addTo(this.layerGroup!);
      marker.bindPopup(`${this.selectedSensorType}: ${value.toFixed(2)}`);
    });
  }

  private renderAnomaliesOverlay(authHeader: string): void {
    if (!this.selectedSensor) return;
    this.analyticsService.getAnomalies(this.selectedSensor, authHeader, this.selectedSensorType, 3.0)
      .subscribe({
        next: (anomalies) => {
          anomalies?.forEach(a => {
            if (a.latitude && a.longitude) this.addAnomalyMarker(a.latitude, a.longitude, (a.payload as any)[this.selectedSensorType]);
          });
        },
        error: (err) => console.error("Errore anomalie:", err)
      });
  }

  private addAnomalyMarker(lat: number, lng: number, value: any): void {
    const icon = L.divIcon({
      className: 'anomaly-pulse',
      html: `<div style="background:#ff0000; width:18px; height:18px; border-radius:50%; border:2px solid white; box-shadow: 0 0 10px #ff0000;"></div>`,
      iconSize: [18, 18],
      iconAnchor: [9, 9]
    });
    L.marker([lat, lng], { icon }).addTo(this.layerGroup!)
      .bindPopup(`<b style="color:red">ANOMALIA RILEVATA</b><br>Valore: ${value}`);
  }

  // ========================================================================
  //  CHARTS & ANALYTICS
  // ========================================================================

  private updateTrendChart(): void {
    const canvas = this.trendChartCanvas?.nativeElement;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // 1. Selezione sorgente dati
    const dataSource = this.showTrend ? this.sensorTrendLocalList : this.sensorDataLocalList;

    if (!dataSource || dataSource.length === 0) {
      if (this.trendChart) { this.trendChart.destroy(); this.trendChart = undefined; }
      return;
    }

    // 2. Filtro e Ordinamento per il sensore selezionato
    const targetId = String(this.selectedSensor || '').trim();
    const sensorSpecificData = dataSource
      .filter(d => String(d.sensorId || '').trim() === targetId)
      .sort((a, b) => new Date(a.timestamp!).getTime() - new Date(b.timestamp!).getTime());

    if (sensorSpecificData.length === 0) {
      if (this.trendChart) { this.trendChart.destroy(); this.trendChart = undefined; }
      return;
    }

    // 3. Preparazione Label
    const historyLabels = sensorSpecificData.map(d =>
      new Date(d.timestamp!).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    );

    // 4. CALCOLO MEDIA MOBILE (Smoothing)
    // Questo risolve il problema delle fluttuazioni eccessive
    const windowSize = 30;// Ogni 5 punti calcola la media. Aumenta a 8-10 per più morbidezza.
    const historyValues = sensorSpecificData.map((d, index, arr) => {
      const rawValue = (d.payload as any)?.[this.selectedSensorType] ?? 0;

      if (index < windowSize - 1) return rawValue; // Non medi messi i primi punti per non sballare l'inizio

      const slice = arr.slice(index - windowSize + 1, index + 1);
      const sum = slice.reduce((acc, curr) => acc + ((curr.payload as any)?.[this.selectedSensorType] ?? 0), 0);
      return sum / windowSize;
    });

    // 5. Preparazione Dataset Forecast
    let finalLabels = [...historyLabels];
    let forecastDataset: any = null;

    if (this.isForecast && this.predictionValue !== null) {
      finalLabels.push('Forecast');
      const fValues = new Array(historyValues.length).fill(null);
      // Collega il forecast all'ultimo valore SMUSSO (per continuità visiva)
      fValues[historyValues.length - 1] = historyValues[historyValues.length - 1];
      fValues.push(this.predictionValue);

      forecastDataset = {
        label: 'Previsione',
        data: fValues,
        borderColor: '#ff4d4d',
        borderDash: [5, 5],
        backgroundColor: 'transparent',
        fill: false,
        tension: 0.4,
        pointRadius: 5,
        pointBackgroundColor: '#ff4d4d',
        order: 1
      };
    }

    // 6. Rendering Finale
    if (this.trendChart) this.trendChart.destroy();

    this.trendChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: finalLabels,
        datasets: [
          {
            label: this.selectedSensorType,
            data: historyValues,
            borderColor: '#0dcaf0',
            backgroundColor: 'rgba(13, 202, 240, 0.2)',
            fill: true,
            tension: 0.4,
            pointRadius: historyValues.length <= 1 ? 8 : 3,
            pointBackgroundColor: '#0dcaf0',
            order: 2
          },
          ...(forecastDataset ? [forecastDataset] : [])
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: {
          legend: {
            display: true,
            labels: { color: '#8a8d98', font: { size: 10 } }
          },
          tooltip: { enabled: true }
        },
        scales: {
          x: {
            ticks: { color: '#8a8d98', font: { size: 10 } },
            grid: { display: false }
          },
          y: {
            ticks: { color: '#8a8d98', font: { size: 10 } },
            grid: { color: 'rgba(255,255,255,0.05)' },
            grace: '15%' // Evita che i punti tocchino i bordi superiore/inferiore
          }
        }
      }
    });
  }

  private loadPrediction(): void {
    const token = this.cookieService.get('token');
    if (!this.selectedSensor) return;
    this.analyticsService.getPrediction(this.selectedSensor, `Bearer ${token}`, this.selectedSensorType)
      .subscribe({
        next: (res) => {
          this.predictionValue = res['predictedValue'];
          this.updateTrendChart();
        },
        error: (err) => console.error("Errore predizione:", err)
      });
  }

  // ========================================================================
  //  UI ACTIONS
  // ========================================================================

  onSensorSelect(sensor: SensorDto): void {
    if (this.selectedSensor === sensor.id) {
      this.selectedSensor = undefined;
    } else {
      this.selectedSensor = sensor.id;
      const data = this.sensorDataLocalList.find(d => d.sensorId === sensor.id);
      if (data?.latitude && data?.longitude) this.map?.setView([data.latitude, data.longitude], 14);
    }
    this.updateGrid();
    if (this.isForecast && this.selectedSensor) this.loadPrediction();
  }

  onSensorTypeSelect(type: string): void {
    this.selectedSensorType = type;
    this.updateGrid();
    if (this.isForecast && this.selectedSensor) this.loadPrediction();

  }


  onForecastIntervalSelect(): void { if (this.selectedSensor) this.loadPrediction(); }

  onLatestIntervalSelect(): void {
    this.cachedData.clear();
    const interval = parseInt(this.selectedInterval?.toString(), 10);
    if (isNaN(interval)) return;

    const obs = this.getIntervalObservable(interval);
    if (obs) {
      obs.subscribe({
        next: (res: any) => {
          if (res?.sensorData) {
            this.sensorDataLocalList = res.sensorData;
            this.sensorTypeList = res.sensorAreaTypes;
            this.updateGrid();
          }
        },
        error: (err) => console.error("Errore refresh:", err)
      });
    }
  }

  private getIntervalObservable(interval: number) {
    if (!this.id) return null;
    if (this.isRealTime) {
      if (interval === 5) return this.sensorDataService.getAllPrivateSensorDataByInterestAreaId5Min(this.id);
      if (interval === 10) return this.sensorDataService.getAllPrivateSensorDataByInterestAreaId10Min(this.id);
      if (interval === 15) return this.sensorDataService.getAllPrivateSensorDataByInterestAreaId15Min(this.id);
      return null;
    }
    return this.sensorDataService.getLastPrivateSensorDataByInterestAreaId(this.id);
  }

  onDateRangeSubmit(): void {
    this.cachedData.clear();
    const defaultDate = new Date().toISOString().split('T')[0];
    const dateDto: DateDto = {
      form: `${this.startDate || defaultDate}T${this.startHour || '00'}:00:00`,
      to: `${this.endDate || defaultDate}T${this.endHour || '23'}:59:59`,
      sensorId: this.selectedSensor,
      interestAreaId: this.id!,
      token: this.cookieService.get('token')
    };

    this.sensorDataService.getAllPrivateSensorDataBySensorBetweenDate(dateDto).subscribe({
      next: (data) => {
        this.sensorDataLocalList = data.sensorData!;
        this.updateGrid();
        if (this.isForecast && this.selectedSensor) this.loadPrediction();
      },
      error: (err) => this.snackBar.open("Errore nel recupero dati", "OK", { duration: 3000 })
    });
  }

  // ========================================================================
  //  HELPERS
  // ========================================================================

  private initializeMap(): void {
    this.map = L.map('map').setView([45.0, 7.0], 5);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { attribution: '© OSM' }).addTo(this.map);
    this.map.on('moveend', () => this.updateGrid());
  }

  private drawInterestArea(geometry: string): void {
    try {
      const geoJson = parse(geometry.trim().replace(/;$/, ''));
      this.removeDrawnAreas();
      if (geoJson && (geoJson.type === 'Polygon' || geoJson.type === 'MultiPolygon')) {
        const polygon = L.geoJSON(geoJson, { style: { color: 'blue', weight: 4, opacity: 0.7 } }).addTo(this.map!);
        this.drawnLayers.push(polygon);
        this.map!.fitBounds(polygon.getBounds());
      }
    } catch (e) { console.error(e); }
  }

  private removeDrawnAreas(): void {
    this.drawnLayers.forEach(l => this.map?.removeLayer(l));
    this.drawnLayers = [];
  }

  private getColor(value: number): string {
    const ratio = Math.min(Math.max((value - 0) / 100, 0), 1);
    return `rgb(${Math.round(255 * ratio)}, 0, ${Math.round(255 * (1 - ratio))})`;
  }

  togglePanel(event: MouseEvent): void {
    event.stopPropagation();
    this.isPanelVisible = !this.isPanelVisible;
  }

  openChartModal(): void {
    if (!this.trendChart || !this.trendChart.data) return;

    this.dialog.open(TrendChartModalComponent, {
      width: '90vw',
      height: '80vh',
      maxWidth: '1200px',
      panelClass: 'trend-modal-container',
      data: {
        labels: this.trendChart.data.labels,
        datasets: this.trendChart.data.datasets,
        sensorType: this.selectedSensorType
      }
    });
  }
}
