import {AfterViewInit, Component, OnDestroy} from '@angular/core';
import * as L from 'leaflet';
import { HttpClient } from '@angular/common/http';
import { ToolbarComponent } from '../../toolbar/toolbar.component';

import 'leaflet.markercluster';
import { SensorDto } from '../../../../model/sensorDto';
import { UserService } from '../../../../service/user.service';
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";
import {NgForOf, NgIf} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {SensorDataService} from "../../../../service/sensorData.service";

@Component({
  selector: 'app-obsmap',
  standalone: true,
  imports: [ToolbarComponent, MatRadioButton, MatRadioGroup, NgForOf, NgIf, ReactiveFormsModule, FormsModule,
// TODO: `HttpClientModule` should not be imported into a component directly.
// Please refactor the code to add `provideHttpClient()` call to the provider list in the
// application bootstrap logic and remove the `HttpClientModule` import from this component.
  ],
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements AfterViewInit, OnDestroy {
  constructor(private http: HttpClient, private userService: UserService, private sensorDataService: SensorDataService) {}

  private obsmap!: L.Map;
  sensorOptions = [
    { label: 'Temperature', value: 'temperature', selected: false },
    { label: 'CO2', value: 'CO2', selected: false },
    { label: 'Humidity', value: 'humidity', selected: false },
    { label: 'Pression', value: 'ap', selected: false },
  ];
  public selectedSensorType: string = "CO2";
  selectedLatestInterval: string | null = null;
  selectedForecastInterval: string | null = null;
  private layerGroup: L.LayerGroup | undefined;
  temperatureScale = [
    {label: '-10', color: '#0030ff'},
    {label: '-8', color: '#0066ff'},
    {label: '-6', color: '#00a4ff'},
    {label: '-4', color: '#00d7ff'},
    {label: '-2', color: '#00f9ed'},
    {label: '0', color: '#00ebbd'},
    {label: '2', color: '#00dc8d'},
    {label: '4', color: '#00c951'},
    {label: '6', color: '#01ba1c'},
    {label: '8', color: '#21bd05'},
    {label: '10', color: '#61cf03'},
    {label: '12', color: '#93df01'},
    {label: '14', color: '#cff000'},
    {label: '16', color: '#ffff00'},
    {label: '18', color: '#ffed00'},
    {label: '20', color: '#ffd700'},
    {label: '22', color: '#ffc400'},
    {label: '24', color: '#ffaf00'},
    {label: '26', color: '#ff9200'},
    {label: '28', color: '#ff7100'},
    {label: '30', color: '#ff4700'},
    {label: '32', color: '#ff2300'},
    {label: '34', color: '#ff0100'},
    {label: '36', color: '#de0014'},
    {label: '38', color: '#bd0033'},
    {label: '40', color: '#940056'},
    {label: '42', color: '#730073'}
  ];
  private cachedData: Map<string, any> = new Map(); // Cache for sensor data
  private markerClusterGroup!: L.MarkerClusterGroup;
  private sensorCountPerMarker: { [key: string]: number } = {};

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

    return L.divIcon({
      className: 'custom-marker-icon', // Classe opzionale per lo styling vediamo se serve e funziona (per ora no)
      html: markerIconDiv
    });
  }


  ngOnDestroy(): void {
    if (this.obsmap) {
      this.obsmap.remove();
    }
  }

  onSensorTypeChange(event: any) {
    this.selectedSensorType = event.value;
    this.loadSensorData()
  }


  togglePanel(event: MouseEvent): void {
    event.stopPropagation();
    this.isPanelVisible = !this.isPanelVisible;
  }



  private createClusterIcon(cluster: L.MarkerCluster): L.DivIcon {
    const childCount = cluster.getChildCount();
    const size = childCount < 10 ? 'small' : childCount < 100 ? 'medium' : 'large';
    const iconSize = size === 'small' ? '30px' : size === 'medium' ? '40px' : '50px';
    const fontSize = size === 'small' ? '14px' : size === 'medium' ? '18px' : '22px';

    const markerIconDiv = document.createElement('div');
    markerIconDiv.style.width = iconSize;
    markerIconDiv.style.height = iconSize;
    markerIconDiv.style.borderRadius = '50%';
    markerIconDiv.style.backgroundColor = 'green';
    markerIconDiv.style.display = 'flex';
    markerIconDiv.style.justifyContent = 'center';
    markerIconDiv.style.alignItems = 'center';
    markerIconDiv.style.color = 'white';
    markerIconDiv.style.fontSize = fontSize;
    markerIconDiv.innerText = childCount.toString();

    return L.divIcon({
      className: 'custom-cluster-icon', // Classe opzionale per lo styling vediamo se serve e funziona (per ora no)
      html: markerIconDiv
    });
  }



  private initMap(): void {
    this.obsmap = L.map('map').setView([41.8719, 12.5674], 5);
    this.obsmap.setMaxZoom(13); // Imposta il livello di zoom massimo a 9
    this.obsmap.setMinZoom(5); // Imposta il livello di zoom minimo a 5

    const tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    });
    tileLayer.addTo(this.obsmap);

    this.markerClusterGroup = L.markerClusterGroup({
      spiderfyOnMaxZoom: true,
      showCoverageOnHover: false,
      maxClusterRadius: 40,
      iconCreateFunction: this.createClusterIcon //crea le icone raggruppate
    });
    this.markerClusterGroup.addTo(this.obsmap);
    this.obsmap.on('zoomend', this.onZoomEnd); //Triggher dello zoom
  }


  private loadSensorData(): void {
    if (this.cachedData.has(this.selectedSensorType)) {
      this.updateGrid();
    } else {
      this.sensorDataService.getProcessedSensorData(this.selectedSensorType)
        .subscribe(response => {
          let geoJson: any;

          // Effettua il parsing della risposta se è una stringa
          if (typeof response === 'string') {
            try {
              geoJson = JSON.parse(response);
            } catch (error) {
              console.error('Errore nel parsing del JSON:', error);
              return;
            }
          } else {
            geoJson = response;
          }

          // Verifica che geoJson abbia la struttura corretta
          if (geoJson && Array.isArray(geoJson.features)) {
            const heatData = geoJson.features.map((feature: any) => [
              feature.geometry.coordinates[1],
              feature.geometry.coordinates[0],
              feature.properties.value
            ] as [number, number, number]);

            this.cachedData.set(this.selectedSensorType, heatData);
            this.updateGrid();
          } else {
            console.error('Formato della risposta non valido:', geoJson);
          }
        });
    }
  }

  private updateGrid(): void {
    if (this.layerGroup) {
      this.layerGroup.clearLayers(); // Remove existing layers if present
    }
    const heatData = this.cachedData.get(this.selectedSensorType);
    if (heatData) {
      this.addPointsToMap(heatData);
    }
  }

  private addPointsToMap(heatData: [number, number, number][]): void {
    heatData.forEach(dataPoint => {
      const [lat, lng, value] = dataPoint;
      const marker = L.circleMarker([lat, lng], {
        radius: 8,
        fillColor: this.getColorScale()(value),
        color: '#000',
        weight: 1,
        opacity: 1,
        fillOpacity: 0.8
      }).addTo(this.layerGroup!);
      marker.bindPopup(`Value: ${value}`);
    });
  }

  private getColorScale() {
    return (value: number) => {
      const min = 0, max = 100; // Adjust these values based on your data range
      const ratio = (value - min) / (max - min);
      const r = Math.round(255 * ratio);
      const g = 0;
      const b = Math.round(255 * (1 - ratio));
      return `rgb(${r},${g},${b})`;
    };
  }


  private onZoomEnd = () => {
    if (this.markerClusterGroup) {
      this.markerClusterGroup.refreshClusters();
    }
  };
  isPanelVisible= true;

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
      this.loadSensorData();
    }, 10);
  }

  onLatestIntervalSelect($event: Event) {
    const selectElement = event?.target as HTMLSelectElement;
    this.selectedLatestInterval = selectElement.value;

    this.selectedForecastInterval = null;
    (document.getElementById('forecastInterval') as HTMLSelectElement).value = '';
    this.loadSensorData()
  }
}
