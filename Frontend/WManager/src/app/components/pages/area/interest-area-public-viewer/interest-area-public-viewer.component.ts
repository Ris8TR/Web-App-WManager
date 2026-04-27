import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { InterestAreaDto } from "../../../../model/interestAreaDto";
import { InterestAreaDataService } from "../../../../service/InterestAreaDataService";
import { NgClass, NgForOf, NgIf } from "@angular/common";
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

@Component({
  selector: 'app-interest-area-public-viewer',
  standalone: true,
  imports: [NgIf, ToolbarComponent, FormsModule, NgForOf, NgClass],
  templateUrl: './interest-area-public-viewer.component.html',
  styleUrl: './interest-area-public-viewer.component.css'
})
export class InterestAreaPublicViewerComponent implements OnInit {
  interestArea!: InterestAreaDto | null;
  @ViewChild('forecastInterval', { static: false }) forecastIntervalElement!: ElementRef<HTMLSelectElement>;

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
    private interestAreaDataService: InterestAreaDataService
  ) {}

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

      this.snackBar.open(`Caricamento previsioni per +${selectedInterval} ore...`, "OK", { duration: 2000 });
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
    this.addPointsToMap(this.cachedData.get(this.selectedSensorType) || []);
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
        style: { color: 'blue', weight: 4, opacity: 0.7 }
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
        this.snackBar.open("No data for this sensor", "OK", { duration: 2000 });
      }
    }
  }

  private getIntervalObservable(interval: number) {
    if (!this.id) return null;
    if (this.isRealTime) {
      switch (interval) {
        case 5: return this.sensorDataService.getAllPrivateSensorDataByInterestAreaId5Min(this.id);
        case 10: return this.sensorDataService.getAllPrivateSensorDataByInterestAreaId10Min(this.id);
        case 15: return this.sensorDataService.getAllPrivateSensorDataByInterestAreaId15Min(this.id);
        default: return null;
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
}
