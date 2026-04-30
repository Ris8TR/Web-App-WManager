import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { NgClass, NgForOf, NgIf } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { MatRadioButton, MatRadioGroup } from "@angular/material/radio";

// Leaflet & Plugins
import * as L from 'leaflet';
import 'leaflet.markercluster';
import 'leaflet.heat';
import { parse } from "terraformer-wkt-parser";

// Models & Services
import { SensorData } from "../../../../model/sensorData";
import { SensorDataInterestAreaDto } from "../../../../model/SensorDataInterestAreaDto";
import { InterestAreaDto } from "../../../../model/interestAreaDto";
import { SensorDataService } from "../../../../service/sensorData.service";
import { InterestAreaService } from "../../../../service/interestArea.service";
import { UserService } from '../../../../service/user.service';

@Component({
  selector: 'app-obsmap',
  standalone: true,
  imports: [MatRadioButton, MatRadioGroup, NgForOf, NgIf, ReactiveFormsModule, FormsModule, NgClass],
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit, AfterViewInit, OnDestroy {
  // --- CONFIGURATION & CONSTANTS ---
  private readonly SENSOR_RANGES: Record<string, { min: number; max: number }> = {
    temperature: { min: -10, max: 40 },
    CO2: { min: 300, max: 2000 },
    humidity: { min: 0, max: 100 },
    ap: { min: 950, max: 1050 }
  };

  public readonly SENSOR_OPTIONS = [
    { label: 'Temperature', value: 'temperature' },
    { label: 'CO2', value: 'CO2' },
    { label: 'Humidity', value: 'humidity' },
    { label: 'Pression', value: 'ap' },
  ];

  public readonly TEMPERATURE_SCALE = [
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

  // --- STATE ---
  public selectedSensorType: string = "CO2";
  public selectedInterval: number = 5;
  public isPanelVisible = true;
  protected isRealTime = false;
  public sensorTypeList: string[] = [];
  public interestAreas: InterestAreaDto[] | undefined;

  // --- LEAFLET LAYERS ---
  private map!: L.Map;
  private heatLayer: any;
  private layerGroup: L.LayerGroup = L.layerGroup();
  private drawnLayers: L.Layer[] = [];
  private cachedData: Map<string, any> = new Map();
  private sensorDataLocalList: Array<SensorData> | undefined;

  constructor(
    private sensorDataService: SensorDataService,
    private interestAreaService: InterestAreaService,
    private userService: UserService, // Mantenuto per compatibilità
    private http: HttpClient          // Mantenuto per compatibilità
  ) { }

  // --- LIFECYCLE ---
  ngOnInit(): void { }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
      this.map.invalidateSize();
      this.loadInterestAreas();
      this.loadSensorData();
    }, 15);
  }

  ngOnDestroy(): void {
    if (this.map) this.map.remove();
  }

  // --- MAP INITIALIZATION ---
  private initMap(): void {
    this.map = L.map('map', { preferCanvas: true }).setView([41.8719, 12.5674], 5);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);
    // Dopo la creazione della mappa, definisci un pane
    this.map.createPane('heatmapPane');
    this.map.getPane('heatmapPane')!.style.zIndex = '100';

    this.layerGroup.addTo(this.map);
  }

  // --- DATA ORCHESTRATION ---
  private loadSensorData(): void {
    if (this.isRealTime || !this.map) return;

    this.sensorDataService.getAllPublicSensorDataOnTop().subscribe({
      next: (res: SensorDataInterestAreaDto) => {
        if (res?.sensorData) {
          this.sensorTypeList = res.sensorAreaTypes || [];
          this.sensorDataLocalList = res.sensorData;
          this.updateMapVisuals();
        }
      },
      error: (err) => console.error('Errore caricamento sensori:', err)
    });
  }

  onLatestIntervalSelect(): void {
    if (!this.selectedInterval) return;

    const interval = parseInt(this.selectedInterval.toString(), 10);
    const obs = this.getIntervalObservable(interval);

    if (obs) {
      obs.subscribe({
        next: (res: any) => {
          if (res) {
            this.sensorTypeList = res.sensorAreaTypes || [];
            this.sensorDataLocalList = res.sensorData;
            this.updateMapVisuals();
          }
        },
        error: (err) => console.error('Errore intervallo:', err)
      });
    }
  }

  onSensorTypeSelect(type: string): void {
    if (this.selectedSensorType === type) return;
    this.selectedSensorType = type;
    if (this.sensorDataLocalList) this.updateMapVisuals();
  }

  private updateMapVisuals(): void {
    if (!this.sensorDataLocalList || !this.map) return;

    const heatData = this.extractHeatData(this.sensorDataLocalList);
    this.cachedData.set(this.selectedSensorType, heatData);

    this.drawHeatmap(heatData);
    this.drawMarkers(this.sensorDataLocalList);

  }

  private getIntervalObservable(interval: number) {
    if (this.isRealTime) {
      if (interval === 5) return this.sensorDataService.getAllPublicSensorDataIn5Min();
      if (interval === 10) return this.sensorDataService.getAllPublicSensorDataIn10Min();
      if (interval === 15) return this.sensorDataService.getAllPublicSensorDataIn15Min();
      return null;
    }
    return this.sensorDataService.getAllPublicSensorDataOnTop();
  }

  // --- VISUALIZATION LOGIC ---

 private drawHeatmap(heatData: [number, number, number][]): void {
   /*if (this.heatLayer) this.map.removeLayer(this.heatLayer);
    this.heatLayer = L.heatLayer(heatData, {
      radius: 40,
      blur: 80,
      maxZoom: 8,
      gradient: this.getHeatmapGradient()
    }).addTo(this.map);*/
  }

  private drawMarkers(sensorDataList: SensorData[]): void {
    this.layerGroup.clearLayers();

    sensorDataList.forEach(data => {
      if (data.latitude == null || data.longitude == null) return;

      const val = data.payload ? (data.payload[this.selectedSensorType] ?? 0) : 0;
      const color = this.getColorForValue(val);

      const marker = L.circleMarker([data.latitude, data.longitude], {
        radius: 6,
        fillColor: color,
        color: "#ffffff",
        weight: 1,
        fillOpacity: 0.9,
        pane: 'overlayPane'
      });

      marker.bindPopup(`
        <div style="font-family: sans-serif; min-width: 120px;">
          <div style="font-size: 10px; color: #666; text-transform: uppercase;">Sensor ID</div>
          <div style="font-size: 14px; font-weight: bold;">${data.sensorId}</div>
          <hr style="margin: 5px 0; border: 0; border-top: 1px solid #eee;">
          <div style="font-size: 10px; color: #666; text-transform: uppercase;">${this.selectedSensorType}</div>
          <div style="font-size: 18px; font-weight: bold; color: ${color};">${val}</div>
        </div>
      `);

      marker.addTo(this.layerGroup);
    });
  }

  private extractHeatData(list: SensorData[]): [number, number, number][] {
    const range = this.SENSOR_RANGES[this.selectedSensorType] || { min: 0, max: 100 };
    return list.map(d => {
      const val = d.payload ? (d.payload[this.selectedSensorType] || 0) : 0;
      const intensity = Math.min(Math.max((val - range.min) / (range.max - range.min), 0), 1);
      return [d.latitude!, d.longitude!, intensity];
    }).filter(p => p[0] !== null && p[1] !== null) as [number, number, number][];
  }

  private getHeatmapGradient() {
    const gradient: any = {};
    const range = this.SENSOR_RANGES[this.selectedSensorType] || { min: 0, max: 100 };

    this.TEMPERATURE_SCALE.forEach(item => {
      const ratio = (parseFloat(item.label) - range.min) / (range.max - range.min);
      const clampedRatio = Math.min(Math.max(ratio, 0), 1);
      gradient[clampedRatio.toFixed(2)] = item.color;
    });
    return gradient;
  }

  private getColorForValue(value: number): string {
    const range = this.SENSOR_RANGES[this.selectedSensorType] || { min: 0, max: 100 };
    const ratio = Math.min(Math.max((value - range.min) / (range.max - range.min), 0), 1);
    const hue = (1 - ratio) * 240; // Da Blu (240) a Rosso (0)
    return `hsl(${hue}, 80%, 45%)`;
  }

  // --- INTEREST AREAS ---
  private loadInterestAreas(): void {
    this.interestAreaService.getAllPublicInterestArea().subscribe(areas => {
      this.interestAreas = areas;
      if (areas) this.drawInterestAreas(areas);
    });
  }

  private drawInterestAreas(areas: InterestAreaDto[]): void {
    this.removeDrawnAreas();
    areas.forEach(area => {
      try {
        const geometry = area.geometry?.trim().replace(/;$/, '');
        if (!geometry) return;

        const geoJson = parse(geometry);
        const layer = L.geoJSON(geoJson, {
          style: { color: 'blue', weight: 4, opacity: 0.7 }
        }).bindPopup(`Area di interesse: ${area.name}`);

        layer.addTo(this.map!);
        this.drawnLayers.push(layer);
      } catch (e) {
        console.error('Parsing error area:', area.name, e);
      }
    });

    if (this.drawnLayers.length > 0) {
      this.map.fitBounds(L.featureGroup(this.drawnLayers).getBounds());
    }
  }

  private removeDrawnAreas(): void {
    this.drawnLayers.forEach(l => this.map.removeLayer(l));
    this.drawnLayers = [];
  }

  // --- UI HELPERS ---
  onSensorTypeChange(): void {
    const el = document.getElementById('type') as HTMLSelectElement;
    if (el) this.onSensorTypeSelect(el.value);
  }

  togglePanel(event: MouseEvent): void {
    event.stopPropagation();
    this.isPanelVisible = !this.isPanelVisible;
  }
}
