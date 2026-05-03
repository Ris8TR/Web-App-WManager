import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { InterestAreaDto } from "../../../../model/interestAreaDto";
import { InterestAreaDataService } from "../../../../service/InterestAreaDataService";
import {DecimalPipe, NgClass, NgForOf, NgIf} from "@angular/common";
import { FormsModule } from "@angular/forms";
import * as L from "leaflet";
import { SensorDto } from "../../../../model/sensorDto";
import { Subscription } from "rxjs";
import { SensorData } from "../../../../model/sensorData";
import { SensorDataService } from "../../../../service/sensorData.service";
import { InterestAreaService } from "../../../../service/interestArea.service";
import { SensorService } from "../../../../service/sensor.service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { CookieService } from "ngx-cookie-service";
import { ToolbarComponent } from "../../../elements/toolbar/toolbar.component";
import { ActivatedRoute } from "@angular/router";
import { parse } from "terraformer-wkt-parser";
import { DateDto } from "../../../../model/dateDto";
import { SensorDataInterestAreaDto } from "../../../../model/SensorDataInterestAreaDto";
import Chart from "chart.js/auto";
import {TrendChartModalComponent} from "../../../elements/trend-chart-modal/trend-chart-modal.component";
import {MatDialog} from "@angular/material/dialog";
import {AnalyticService} from "../../../../service/analytic.service";

@Component({
  selector: 'app-interest-area-public-viewer',
  standalone: true,
  imports: [NgIf, ToolbarComponent, FormsModule, NgForOf, NgClass, DecimalPipe],
  templateUrl: './interest-area-public-viewer.component.html',
  styleUrl: './interest-area-public-viewer.component.css'
})
export class InterestAreaPublicViewerComponent implements OnInit {
  interestArea!: InterestAreaDto | null;
  @ViewChild('forecastInterval', {static: false}) forecastIntervalElement!: ElementRef<HTMLSelectElement>;
  @ViewChild('trendChart') trendChartCanvas!: ElementRef<HTMLCanvasElement>;

  private map: L.Map | undefined;
  selectedSensor!: string | undefined;
  isRealTime: boolean = false;
  id: string | null | undefined;
  private layerGroup: L.LayerGroup | undefined;
  public selectedSensorType: string = "CO2";
  sensorTypeList!: string[];
  public sensors: SensorDto[] = [];
  public startDate?: string;
  public endDate?: string;
  public startHour?: string;
  public endHour?: string;
  public temperatureScale = [
    {label: '-10', color: '#0030ff'}, {label: '-8', color: '#0066ff'},
    {label: '-6', color: '#00a4ff'}, {label: '-4', color: '#00d7ff'},
    {label: '-2', color: '#00f9ed'}, {label: '0', color: '#00ebbd'},
    {label: '2', color: '#00dc8d'}, {label: '4', color: '#00c951'},
    {label: '6', color: '#01ba1c'}, {label: '8', color: '#21bd05'},
    {label: '10', color: '#61cf03'}, {label: '12', color: '#93df01'},
    {label: '14', color: '#cff000'}, {label: '16', color: '#ffff00'},
    {label: '18', color: '#ffed00'}, {label: '20', color: '#ffd700'},
    {label: '22', color: '#ffc400'}, {label: '24', color: '#ffaf00'},
    {label: '26', color: '#ff9200'}, {label: '28', color: '#ff7100'},
    {label: '30', color: '#ff4700'}, {label: '32', color: '#ff2300'},
    {label: '34', color: '#ff0100'}, {label: '36', color: '#de0014'},
    {label: '38', color: '#bd0033'}, {label: '40', color: '#940056'},
    {label: '42', color: '#730073'}
  ];

  // --- Analytics State ---
  public showTrend = false;
  public showAnomalies = false;
  public predictionValue: number | null = null;
  protected isForecast = false;
  protected isObservation = false;
  private trendChart: Chart | undefined;

  public logStringResult: string = 'Login';
  // --- Data Storage ---
  private sensorTrendLocalList: SensorData[] = [];


  // Cache per i punti della mappa: [lat, lng, valore]
  private cachedData: Map<string, [number, number, number][]> = new Map();
  private drawnLayers: L.Layer[] = [];

  isPanelVisible = true;
  private sensorDataLocalList: Array<SensorData> = [];

  constructor(
    private sensorDataService: SensorDataService,
    private interestAreaService: InterestAreaService,
    private sensorService: SensorService,
    private snackBar: MatSnackBar,
    private cookieService: CookieService,
    public toolbarComponent: ToolbarComponent,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private interestAreaDataService: InterestAreaDataService,
    private analyticsService: AnalyticService

  ) {
  }


  ngOnInit(): void {
    if (!this.map) this.initializeMap();
    this.interestAreaDataService.currentArea.subscribe((area) => {
      if (area) {
        this.interestArea = area;
        this.id = area.id; // Imposta l'ID per le query successive
        if (this.interestArea && this.interestArea.geometry) {
          this.drawInterestArea(this.interestArea.geometry);
          this.loadSensors();
          this.loadAllSensorData();
        }
      }
    });
  }

  private initializeMap(): void {
    this.map = L.map('map').setView([45.0, 7.0], 5);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 14,
      minZoom: 5
    }).addTo(this.map);
    this.map.on('moveend', () => this.updateGrid());
  }

  // Helper per estrarre i dati per la mappa senza usare JSON.parse
  private extractHeatData(sensorDataList: SensorData[]): [number, number, number][] {
    const heatData: [number, number, number][] = [];
    sensorDataList.forEach((data) => {
      const lat = data.latitude;
      const lng = data.longitude;

      // Accesso diretto al payload (già oggetto)
      const value = data.payload ? (data.payload[this.selectedSensorType] || 0) : 0;

      if (lat != null && lng != null) {
        heatData.push([lat, lng, value]);
      }
    });
    return heatData;
  }

  private loadAllSensorData(): void {
    if (!this.map || !this.interestArea?.id) return;

    this.sensorDataService.getLastPublicSensorDataByInterestAreaId(this.interestArea.id)
      .subscribe((response: SensorDataInterestAreaDto) => {
        if (response && response.sensorData) {
          this.sensorTypeList = response.sensorAreaTypes || [];
          this.sensorDataLocalList = response.sensorData;

          const heatData = this.extractHeatData(this.sensorDataLocalList);
          this.cachedData.set(this.selectedSensorType, heatData);
          this.updateGrid();
        }
      });
  }

  onSensorTypeSelect(type: string) {
    this.selectedSensorType = type;
    if (this.sensorDataLocalList.length > 0) {
      const heatData = this.extractHeatData(this.sensorDataLocalList);
      this.cachedData.set(this.selectedSensorType, heatData);
      this.updateGrid();
    }
  }

  onLatestIntervalSelect(): void {
    const latestElement = document.getElementById('latestInterval') as HTMLSelectElement | null;
    if (!latestElement) return;

    const interval = parseInt(latestElement.value, 10);
    if (isNaN(interval)) return;

    const intervalObservable = this.getIntervalObservable(interval);
    if (intervalObservable) {
      intervalObservable.subscribe((response: any) => {
        if (response && response.sensorData) {
          this.sensorDataLocalList = response.sensorData;
          this.sensorTypeList = response.sensorAreaTypes;

          const heatData = this.extractHeatData(this.sensorDataLocalList);
          this.cachedData.set(this.selectedSensorType, heatData);
          this.updateGrid();
        }
      });
    }
  }

  onForecastIntervalSelect(): void {
    // Recupera il valore dall'elemento ViewChild
    const selectedInterval = this.forecastIntervalElement.nativeElement.value;

    if (selectedInterval) {
      console.log('Intervallo di previsione selezionato:', selectedInterval);
      // Qui andrà la chiamata al servizio per i dati previsionali (forecast)
      // Esempio: this.sensorDataService.getForecast(this.id, selectedInterval).subscribe(...)

      this.snackBar.open(`Caricamento previsioni per +${selectedInterval} ore...`, "OK", {duration: 2000});
    }
  }

  // Corretto il problema TS2345 in processData
  private processData(data: SensorDataInterestAreaDto): [number, number, number][] {
    if (!data || !data.sensorData) return [];

    this.sensorTypeList = data.sensorAreaTypes || [];
    this.sensorDataLocalList = data.sensorData;

    return this.extractHeatData(this.sensorDataLocalList);
  }

  private updateGrid(): void {
    if (!this.map) return;
    if (this.layerGroup) this.map.removeLayer(this.layerGroup);
    this.layerGroup = L.layerGroup().addTo(this.map);

    // 1. Aggiunge i punti base della mappa (dati attuali)
    this.processAndMapLocalData();

    // 2. Gestione logica Trend
    if (this.showTrend && this.selectedSensor) {
      const key = this.selectedSensorType;
      this.analyticsService.getPublicSensorTrend(this.selectedSensor, key).subscribe({
        next: (dataList: any[]) => {
          this.sensorTrendLocalList = dataList;

          // Se vuoi che la mappa mostri i punti del trend invece di quelli "latest":
          const trendHeatData = dataList
            .filter(d => d.latitude && d.longitude)
            .map((d): [number, number, number] => [
              d.latitude!,
              d.longitude!,
              (d.payload as any)?.[key] ?? 0
            ]);

          // Rimuovi i vecchi punti e aggiungi quelli del trend
          if (this.layerGroup) this.layerGroup.clearLayers();
          this.addPointsToMap(trendHeatData);

          // IMPORTANTE: Chiama il grafico SOLO qui dentro
          this.updateTrendChart();
        },
        error: (err) => {
          console.error("Errore trend:", err);
          this.updateTrendChart(); // Prova a disegnare comunque (con i dati esistenti)
        }
      });
    } else {
      // Se non mostriamo il trend, disegna il grafico con i dati normali
      this.updateTrendChart();
    }
  }


  protected updateTrendChart(): void {
    const canvas = this.trendChartCanvas?.nativeElement;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // --- 1. SORGENTE DATI DINAMICA ---
    // Se showTrend è true, usa la lista del trend, altrimenti la lista locale standard
    const dataSource = this.showTrend ? this.sensorTrendLocalList : this.sensorDataLocalList;

    if (!dataSource || dataSource.length === 0) {
      this.updateGrid();
      if (this.trendChart) {
        this.trendChart.destroy();
        this.trendChart = undefined;
      }
      return;
    }

    // --- 2. FILTRO E ORDINAMENTO ---
    // Assicurati che il targetId sia confrontato correttamente (string vs string)
    const targetId = this.selectedSensor ? String(this.selectedSensor).trim() : "";

    const sensorSpecificData = dataSource
      .filter(d => String(d.sensorId || '').trim() === targetId)
      .sort((a, b) => new Date(a.timestamp!).getTime() - new Date(b.timestamp!).getTime());

    if (sensorSpecificData.length === 0) {
      this.updateGrid();
      if (this.trendChart) {
        this.trendChart.destroy();
        this.trendChart = undefined;
      }
      return;
    }

    // --- 3. PREPARAZIONE LABEL ---
    const historyLabels = sensorSpecificData.map(d =>
      new Date(d.timestamp!).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'})
    );

    // --- 4. CALCOLO VALORI (RAW vs SMOOTHED) ---
    // Calcoliamo prima i valori "grezzi" (raw)
    const rawValues = sensorSpecificData.map(d => (d.payload as any)?.[this.selectedSensorType] ?? 0);

    // Calcoliamo i valori "smussati" (smoothed) usando la media mobile
    const windowSize = 5; // Ridotto da 30 a 5 per rendere il grafico più reattivo su dataset piccoli
    const smoothedValues = sensorSpecificData.map((d, index, arr) => {
      const start = Math.max(0, index - windowSize + 1);
      const slice = arr.slice(start, index + 1);
      const sum = slice.reduce((acc, curr) => acc + ((curr.payload as any)?.[this.selectedSensorType] ?? 0), 0);
      return sum / slice.length;
    });

    // DECISIONE: Se showTrend è true, usiamo i valori smoothed, altrimenti i raw
    const finalValues = this.showTrend ? smoothedValues : rawValues;

    // --- 5. PREPARAZIONE DATASET FORECAST ---
    let finalLabels = [...historyLabels];
    let forecastDataset: any = null;

    if (this.isForecast && this.predictionValue !== null) {
      finalLabels.push('Forecast');
      const fValues = new Array(finalValues.length).fill(null);
      fValues[finalValues.length - 1] = finalValues[finalValues.length - 1];
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

    // --- 6. RENDERING ---
    if (this.trendChart) this.trendChart.destroy();

    this.trendChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: finalLabels,
        datasets: [
          {
            label: this.selectedSensorType,
            data: finalValues, // <--- Qui passiamo i dati scelti (raw o smoothed)
            borderColor: '#0dcaf0',
            backgroundColor: 'rgba(13, 202, 240, 0.2)',
            fill: true,
            tension: this.showTrend ? 0.4 : 0, // Più liscio se è trend, più angolare se è raw
            pointRadius: finalValues.length <= 1 ? 8 : 3,
            pointBackgroundColor: '#0dcaf0',
            order: 2
          },
          ...(forecastDataset ? [forecastDataset] : [])
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        // ... resto delle opzioni invariato ...
        interaction: {mode: 'index', intersect: false},
        plugins: {
          legend: { display: true, labels: {color: '#8a8d98', font: {size: 10}} },
          tooltip: {enabled: true}
        },
        scales: {
          x: { ticks: {color: '#8a8d98', font: {size: 10}}, grid: {display: false} },
          y: { ticks: {color: '#8a8d98', font: {size: 10}}, grid: {color: 'rgba(255,255,255,0.05)'}, grace: '15%' }
        }
      }
    });
  }

  private processAndMapLocalData(): void {
    const heatData = this.sensorDataLocalList
      .filter(d => d.latitude && d.longitude)
      .map((d): [number, number, number] => [ // <--- Aggiungi il tipo di ritorno qui
        d.latitude!,
        d.longitude!,
        (d.payload as any)?.[this.selectedSensorType] ?? 0
      ]);

    this.addPointsToMap(heatData);
  }

  private addPointsToMap(heatData: [number, number, number][]): void {
    heatData.forEach(([lat, lng, value]) => {
      L.circleMarker([lat, lng], {
        radius: 8,
        fillColor: this.getColor(value),
        color: '#000',
        weight: 1,
        opacity: 1,
        fillOpacity: 0.8
      }).bindPopup(`${this.selectedSensorType} value: ${value}`).addTo(this.layerGroup!);
    });
  }

  private getColor(value: number): string {
    const min = 0, max = 100;
    const ratio = Math.min(Math.max((value - min) / (max - min), 0), 1);
    const r = Math.round(255 * ratio);
    const b = Math.round(255 * (1 - ratio));
    return `rgb(${r},0,${b})`;
  }

  // --- RESTO DEI METODI (Logica UI e Geometria) ---

  togglePanel(event: MouseEvent): void {
    event.stopPropagation();
    this.isPanelVisible = !this.isPanelVisible;
  }

  private drawInterestArea(geometry: string | undefined): void {
    if (!geometry || !this.map) return;
    try {
      const geoJson = parse(geometry.trim().replace(/;$/, ''));
      this.removeDrawnAreas();

      const layer = L.geoJSON(geoJson, {
        style: {color: 'blue', weight: 4, opacity: 0.7}
      }).addTo(this.map);

      this.drawnLayers.push(layer);
      this.map.fitBounds((layer as L.FeatureGroup).getBounds());
    } catch (error) {
      console.error('Errore WKT:', error);
    }
  }

  private removeDrawnAreas(): void {
    this.drawnLayers.forEach(l => this.map?.removeLayer(l));
    this.drawnLayers = [];
  }

  private loadSensors(): void {
    if (!this.interestArea?.id) return;
    this.sensorService.findByInterestAreaId(this.interestArea.id)
      .subscribe(sensors => {
        this.sensors = sensors;
        if (this.sensors.length > 0) this.selectedSensor = this.sensors[0].id;
      });
  }

  onSensorSelect(sensor: SensorDto): void {
    if (this.selectedSensor === sensor.id) {
      this.selectedSensor = undefined;
    } else {
      this.selectedSensor = sensor.id;
      const data = this.sensorDataLocalList.find(d => d.sensorId === this.selectedSensor);
      if (data) {
        this.map?.setView([data.latitude, data.longitude], 14);
      } else {
        this.snackBar.open("No data for this sensor", "OK", {duration: 2000});
      }
    }
  }

  private getIntervalObservable(interval: number) {
    if (!this.id) return null;
    if (this.isRealTime) {
      switch (interval) {
        case 5:
          return this.sensorDataService.getAllPrivateSensorDataByInterestAreaId5Min(this.id);
        case 10:
          return this.sensorDataService.getAllPrivateSensorDataByInterestAreaId10Min(this.id);
        case 15:
          return this.sensorDataService.getAllPrivateSensorDataByInterestAreaId15Min(this.id);
        default:
          return null;
      }
    }
    return this.sensorDataService.getLastPrivateSensorDataByInterestAreaId(this.id);
  }

  onDateRangeSubmit(): void {
    this.cachedData.clear();
    const now = new Date();
    const dateDto: DateDto = {
      form: `${this.startDate || now.toISOString().split('T')[0]}T${this.startHour || '00'}:00:00`,
      to: `${this.endDate || now.toISOString().split('T')[0]}T${this.endHour || '23'}:59:00`,
      sensorId: this.selectedSensor,
      interestAreaId: this.id!,
      token: this.cookieService.get('token')
    };

    this.sensorDataService.getAllPublicSensorDataBySensorBetweenDate(dateDto).subscribe(data => {
      const heatData = this.processData(data);
      this.cachedData.set(this.selectedSensorType, heatData);
      this.updateGrid();
    });
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

