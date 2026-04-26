import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import * as L from 'leaflet';
import { HttpClient } from '@angular/common/http';
import { ToolbarComponent } from '../../toolbar/toolbar.component';
import 'leaflet.markercluster';
import { SensorDto } from '../../../../model/sensorDto';
import { UserService } from '../../../../service/user.service';
import { MatRadioButton, MatRadioGroup } from "@angular/material/radio";
import {NgClass, NgForOf, NgIf} from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { SensorDataService } from "../../../../service/sensorData.service";
import { SensorData } from "../../../../model/sensorData";
import { parse } from "terraformer-wkt-parser";
import { InterestAreaService } from "../../../../service/interestArea.service";
import { InterestArea } from "../../../../model/interestArea";
import { InterestAreaDto } from "../../../../model/interestAreaDto";
import { SensorDataInterestAreaDto } from "../../../../model/SensorDataInterestAreaDto";

@Component({
  selector: 'app-obsmap',
  standalone: true,
  imports: [MatRadioButton, MatRadioGroup, NgForOf, NgIf, ReactiveFormsModule, FormsModule, NgClass],
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit, AfterViewInit, OnDestroy {
  protected isRealTime = false;

  constructor(
    private http: HttpClient,
    private interestAreaService: InterestAreaService,
    private userService: UserService,
    private sensorDataService: SensorDataService
  ) { }

  private map!: L.Map;
  sensorOptions = [
    { label: 'Temperature', value: 'temperature', selected: false },
    { label: 'CO2', value: 'CO2', selected: false },
    { label: 'Humidity', value: 'humidity', selected: false },
    { label: 'Pression', value: 'ap', selected: false },
  ];
  public selectedSensorType: string = "CO2";
  selectedLatestInterval: string | null = null;
  selectedForecastInterval: string | null = null;
  sensorTypeList!: string[];
  private drawnLayers: L.Layer[] = [];
  interestAreas: InterestAreaDto[] | undefined;
  private sensorDataLocalList: Array<SensorData> | undefined;
  private layerGroup: L.LayerGroup | undefined;
  private markerClusterGroup!: L.MarkerClusterGroup;
  private sensorCountPerMarker: { [key: string]: number } = {};
  private cachedData: Map<string, any> = new Map();
  private sensorGeoJsonLayer!: L.GeoJSON;
  isPanelVisible = true;

  private sensorRanges: { [key: string]: { min: number; max: number } } = {
    temperature: { min: -10, max: 40 },
    CO2: { min: 300, max: 2000 },
    humidity: { min: 0, max: 100 },
    ap: { min: 950, max: 1050 }
  };

  temperatureScale = [
    { label: '-10', color: '#0030ff' }, { label: '-8', color: '#0066ff' },
    { label: '-6', color: '#00a4ff' }, { label: '-4', color: '#00d7ff' },
    { label: '-2', color: '#00f9ed' }, { label: '0', color: '#00ebbd' },
    { label: '2', color: '#00dc8d' }, { label: '4', color: '#00c951' },
    { label: '6', color: '#01ba1c' }, { label: '8', color: '#21bd05' },
    { label: '10', color: '#61cf03' }, { label: '12', color: '#93df01' },
    { label: '14', color: '#cff000' }, { label: '16', color: '#ffff00' },
    { label: '18', color: '#ffed00' }, { label: '20', color: '#ffd700' },
    { label: '22', color: '#ffc400' }, { label: '24', color: '#ffaf00' },
    { label: '26', color: '#ff9200' }, { label: '28', color: '#ff7100' },
    { label: '30', color: '#ff4700' }, { label: '32', color: '#ff2300' },
    { label: '34', color: '#ff0100' }, { label: '36', color: '#de0014' },
    { label: '38', color: '#bd0033' }, { label: '40', color: '#940056' },
    { label: '42', color: '#730073' }
  ];

  radioOptions = [
    { value: false, label: 'Latest' },
    { value: true, label: 'Real-Time' }
  ];

  ngOnInit(): void { }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
      this.map.invalidateSize();  // forza il ricalcolo delle dimensioni
      this.loadInterestAreas();
      this.loadSensorData();
    }, 15);
  }

  ngOnDestroy(): void {
    if (this.map) this.map.remove();
  }

  private initMap(): void {
    if (!this.map) {
      this.map = L.map('map', {
        preferCanvas: true,
      }).setView([41.8719, 12.5674], 5);
      this.map.setMaxZoom(18);
      this.map.setMinZoom(5);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
      }).addTo(this.map);
      this.layerGroup = L.layerGroup().addTo(this.map);
      this.markerClusterGroup = L.markerClusterGroup({
        chunkedLoading: true, // <--- Elabora i marker a blocchi, evitando il freeze
        chunkInterval: 200,   // Millisecondi tra un blocco e l'altro
        maxClusterRadius: 50,
        spiderfyOnMaxZoom: true,
        showCoverageOnHover: false,
        iconCreateFunction: this.createClusterIcon.bind(this)
      }).addTo(this.map);
      this.map.on('zoomend', this.onZoomEnd.bind(this));
    }
  }

  private onZoomEnd = () => {
    if (this.markerClusterGroup) this.markerClusterGroup.refreshClusters();
  };



  private createGreenMarkerIcon(sensorCount: number): L.DivIcon {
    const markerIconDiv = document.createElement('div');
    markerIconDiv.style.width = '30px';
    markerIconDiv.style.height = '30px';
    markerIconDiv.style.borderRadius = '50%';
    markerIconDiv.style.backgroundColor = 'green';
    markerIconDiv.style.display = 'flex';
    markerIconDiv.style.justifyContent = 'center';
    markerIconDiv.style.alignItems = 'center';
    markerIconDiv.style.color = 'white';
    markerIconDiv.style.fontSize = '14px';
    markerIconDiv.innerText = sensorCount.toString();
    return L.divIcon({ className: 'custom-marker-icon', html: markerIconDiv });
  }

  private loadSensorData(): void {
    if (this.isRealTime || !this.map) return;
    this.cachedData.clear();
    this.sensorDataService.getAllPublicSensorDataOnTop().subscribe({
      next: (response: SensorDataInterestAreaDto) => {
        if (response && response.sensorData) {
          this.sensorTypeList = response.sensorAreaTypes || [];
          this.sensorDataLocalList = response.sensorData;
          this.buildGeoJsonLayer(this.sensorDataLocalList);
          const heatData = this.extractHeatData(this.sensorDataLocalList);
          this.cachedData.set(this.selectedSensorType, heatData);
          console.log(`Caricati ${heatData.length} sensori pubblici.`);
        } else {
          console.error('Formato della risposta non valido:', response);
        }
      },
      error: (err) => console.error('Errore durante il caricamento dei dati sensori:', err)
    });
  }

  private buildGeoJsonLayer(sensorDataList: SensorData[]): void {
    if (!this.map || !this.markerClusterGroup) return;

    // --- PULIZIA TOTALE DI OGNI LAYER POSSIBILE ---
    this.markerClusterGroup.clearLayers();
    if (this.layerGroup) this.layerGroup.clearLayers();
    if (this.sensorGeoJsonLayer) this.map.removeLayer(this.sensorGeoJsonLayer);

    const markers: L.CircleMarker[] = [];

    sensorDataList.forEach(data => {
      if (data.latitude == null || data.longitude == null) return;

      const val = data.payload ? (data.payload[this.selectedSensorType] ?? 0) : 0;
      const color = this.getColorForValue(val);

      const marker = L.circleMarker([data.latitude, data.longitude], {
        radius: 8,
        fillColor: color,
        color: "#ffffff",
        weight: 1.5,
        fillOpacity: 0.9,
        className: 'sensor-dot' // Classe CSS per stile extra
      });

      marker.bindPopup(`
            <div style="text-align:center; padding:5px">
                <div style="font-size:12px; color:#666">Sensore ${data.sensorId}</div>
                <div style="font-size:16px; font-weight:bold; color:${color}">
                    ${this.selectedSensorType}: ${val}
                </div>
            </div>
        `);

      markers.push(marker);
    });

    this.markerClusterGroup.addLayers(markers);
  }

// Icona Cluster stilizzata e dinamica
  private createClusterIcon(cluster: L.MarkerCluster): L.DivIcon {
    const count = cluster.getChildCount();

    // Logica colori e dimensioni basata sulla densità
    let color = '#2ecc71'; // Verde (poco denso)
    let size = 35;

    if (count > 50) { color = '#f1c40f'; size = 42; }  // Giallo
    if (count > 200) { color = '#e67e22'; size = 50; } // Arancione
    if (count > 500) { color = '#e74c3c'; size = 60; } // Rosso (molto denso)

    // Usiamo lo stile inline solo per le variabili dinamiche, evitando errori di validazione
    return L.divIcon({
      html: `<div class="custom-cluster-icon" style="background-color: ${color}; width: ${size}px; height: ${size}px;">
                <span>${count}</span>
               </div>`,
      className: '', // Importante: toglie lo stile predefinito di Leaflet
      iconSize: L.point(size, size)
    });
  }

// Scala cromatica professionale (HSL)
  public selectedInterval: number = 5;
  private getColorForValue(value: number): string {
    const range = this.sensorRanges[this.selectedSensorType] || { min: 0, max: 100 };
    let ratio = (value - range.min) / (range.max - range.min);
    ratio = Math.min(Math.max(ratio, 0), 1);

    // Scala da Blu (240) a Rosso (0)
    const hue = (1 - ratio) * 240;
    return `hsl(${hue}, 80%, 45%)`;
  }


  private extractHeatData(sensorDataList: SensorData[]): [number, number, number][] {
    const heatData: [number, number, number][] = [];
    for (const data of sensorDataList) {
      const lat = data.latitude;
      const lng = data.longitude;
      const value = data.payload ? (data.payload[this.selectedSensorType] || 0) : 0;
      if (lat != null && lng != null) heatData.push([lat, lng, value]);
    }
    return heatData;
  }


  onSensorTypeSelect(type: any): void {
    if (this.selectedSensorType === type) return;

    this.selectedSensorType = type;

    if (this.sensorDataLocalList) {
      // Richiama buildGeoJsonLayer che ora pulisce tutto prima di ridisegnare
      this.buildGeoJsonLayer(this.sensorDataLocalList);
    }
  }

  onSensorTypeChange(): void {
    let typeElement = document.getElementById('type') as HTMLSelectElement | null;
    if (typeElement) this.onSensorTypeSelect(typeElement.value);
  }
  onLatestIntervalSelect(): void {
    this.cachedData.clear();

    // 1. Controlla se selectedInterval ha un valore
    if (this.selectedInterval) {
      const interval = parseInt(this.selectedInterval.toString(), 10);

      if (!isNaN(interval)) {
        // 2. Chiama l'observable usando la variabile del componente
        let intervalObservable = this.getIntervalObservable(interval);

        if (intervalObservable) {
          intervalObservable.subscribe({
            next: (response: any) => {
              if (response) {
                console.log('Dati ricevuti per intervallo:', interval, response);

                const sensorDataList = response.sensorData;
                const sensorAreaTypes = response.sensorAreaTypes;

                this.sensorTypeList = sensorAreaTypes;
                this.sensorDataLocalList = sensorDataList;

                // 3. Aggiorna la mappa
                this.buildGeoJsonLayer(sensorDataList);

                const heatData = this.extractHeatData(sensorDataList);
                this.cachedData.set(this.selectedSensorType, heatData);

                this.updateGrid();
              } else {
                console.warn('Nessun dato restituito dall\'observable.');
              }
            },
            error: (error: any) => console.error('Errore durante la sottoscrizione:', error)
          });
        }
      }
    } else {
      console.warn("Nessun intervallo selezionato.");
    }
  }


  private getIntervalObservable(interval: number) {
    if (this.isRealTime) {
      switch (interval) {
        case 5: return this.sensorDataService.getAllPublicSensorDataIn5Min();
        case 10: return this.sensorDataService.getAllPublicSensorDataIn10Min();
        case 15: return this.sensorDataService.getAllPublicSensorDataIn15Min();
        default: return null;
      }
    } else {
      return this.sensorDataService.getAllPublicSensorDataOnTop();
    }
  }

  private updateGrid(): void {
    if (!this.layerGroup) {
      this.layerGroup = L.layerGroup().addTo(this.map);
    }
    this.layerGroup.clearLayers();
    const heatData = this.cachedData.get(this.selectedSensorType);
    if (heatData) this.addPointsToMap(heatData);
  }

  private addPointsToMap(heatData: [number, number, number][]): void {
    if (!this.layerGroup) return;
    for (const [lat, lng, value] of heatData) {
      const marker = L.circleMarker([lat, lng], {
        radius: 8,
        fillColor: this.getColorScale()(value),
        color: '#000',
        weight: 1,
        opacity: 1,
        fillOpacity: 0.8
      }).addTo(this.layerGroup);
      marker.bindPopup(`${this.selectedSensorType} value: ${value}`);
    }
  }

  private getColorScale() {
    return (value: number) => {
      const range = this.sensorRanges[this.selectedSensorType] || { min: 0, max: 100 };
      let ratio = (value - range.min) / (range.max - range.min);
      ratio = Math.min(Math.max(ratio, 0), 1);
      const r = Math.round(255 * ratio);
      const b = Math.round(255 * (1 - ratio));
      return `rgb(${r},0,${b})`;
    };
  }

  togglePanel(event: MouseEvent): void {
    event.stopPropagation();
    this.isPanelVisible = !this.isPanelVisible;
  }

  private loadInterestAreas(): void {
    this.interestAreaService.getAllPublicInterestArea().subscribe(areas => {
      this.interestAreas = areas;
      if (this.interestAreas) this.drawInterestArea(this.interestAreas);
    });
  }

  private drawInterestArea(areas: InterestAreaDto[]): void {
    try {
      this.removeDrawnAreas();
      areas.forEach(area => {
        let geometry = area.geometry?.trim().replace(/;$/, '');
        const geoJson = parse(geometry!);
        if (geoJson && (geoJson.type === 'Polygon' || geoJson.type === 'MultiPolygon')) {
          const polygon = L.geoJSON(geoJson, {
            style: { color: 'blue', weight: 4, opacity: 0.7 }
          }).bindPopup(`Area di interesse: ${area.name}`).addTo(this.map!);
          this.drawnLayers.push(polygon);
        } else if (geoJson && (geoJson.type === 'LineString' || geoJson.type === 'MultiLineString')) {
          const polyline = L.geoJSON(geoJson, {
            style: { color: 'blue', weight: 4, opacity: 0.7 }
          }).bindPopup(`Area di interesse: ${area.name}`).addTo(this.map!);
          this.drawnLayers.push(polyline);
        } else {
          console.error('Tipo di geometria non valido o non supportato:', geoJson?.type);
        }
      });
      if (this.drawnLayers.length > 0) {
        const bounds = L.featureGroup(this.drawnLayers).getBounds();
        this.map!.fitBounds(bounds);
      }
    } catch (error) {
      console.error('Errore durante il parsing delle geometrie:', error);
    }
  }

  private removeDrawnAreas(): void {
    this.drawnLayers.forEach(layer => this.map!.removeLayer(layer));
    this.drawnLayers = [];
  }
}
