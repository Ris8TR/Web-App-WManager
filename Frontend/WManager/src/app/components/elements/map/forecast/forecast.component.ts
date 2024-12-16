import { AfterViewInit, Component, OnDestroy } from '@angular/core';
import * as L from 'leaflet';
import { SensorDataService } from '../../../../service/sensorData.service';
import { FormsModule } from '@angular/forms';
import 'leaflet.heat';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import {NgForOf, NgIf} from "@angular/common";
import {MatCheckbox} from "@angular/material/checkbox";
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";

@Component({
  selector: 'app-forecast',
  standalone: true,
  imports: [FormsModule, MatFormFieldModule, MatSelectModule, NgForOf, NgIf, MatCheckbox, MatRadioGroup, MatRadioButton],
  templateUrl: './forecast.component.html',
  styleUrls: ['./forecast.component.css']
})
export class ForecastComponent implements AfterViewInit, OnDestroy {

  private map: L.Map | undefined;
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
  isPanelVisible = true;

  constructor(private sensorDataService: SensorDataService) {}

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initializeMap();
      this.layerGroup = L.layerGroup().addTo(this.map!);
      this.loadSensorData();
    }, 10);
  }

  togglePanel(event: MouseEvent): void {
    event.stopPropagation();
    this.isPanelVisible = !this.isPanelVisible;
  }



  private initializeMap(): void {
    this.map = L.map('map').setView([45.0, 7.0], 5);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 12,
      minZoom: 5
    }).addTo(this.map);

    this.map.on('moveend', () => {
      this.updateGrid();
    });
  }

  onSensorTypeChange(event: any) {
    this.selectedSensorType = event.value;
    this.loadSensorData()
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

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }



  onForecastIntervalSelect($event: Event) {
    const selectElement = event?.target as HTMLSelectElement;
    this.selectedForecastInterval = selectElement.value;

    this.selectedLatestInterval = null;
    (document.getElementById('latestInterval') as HTMLSelectElement).value = '';
    this.loadSensorData()
  }
}
