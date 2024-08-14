import { AfterViewInit, Component, OnDestroy } from '@angular/core';
import * as L from 'leaflet';
import { SensorDataService } from '../../../../service/sensorData.service';
import { FormsModule } from '@angular/forms';
import 'leaflet.heat';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';

@Component({
  selector: 'app-forecast',
  standalone: true,
  imports: [FormsModule, MatFormFieldModule, MatSelectModule],
  templateUrl: './forecast.component.html',
  styleUrls: ['./forecast.component.css']
})
export class ForecastComponent implements AfterViewInit, OnDestroy {

  private map: L.Map | undefined;
  public sensorType: string = 'CO2'; // Default sensor type
  private layerGroup: L.LayerGroup | undefined;
  private cachedData: Map<string, any> = new Map(); // Cache for sensor data

  constructor(private sensorDataService: SensorDataService) {}

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initializeMap();
      this.layerGroup = L.layerGroup().addTo(this.map!);
      this.loadSensorData();
    }, 10);
  }

  private initializeMap(): void {
    this.map = L.map('map').setView([45.0, 7.0], 5);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 8,
      minZoom: 5
    }).addTo(this.map);

    this.map.on('moveend', () => {
      this.updateGrid();
    });
  }

  onSensorTypeChange(): void {
    console.log('Sensor type changed to: ', this.sensorType);
    this.loadSensorData();
  }

  private loadSensorData(): void {
    if (this.cachedData.has(this.sensorType)) {
      this.updateGrid();
    } else {
      this.sensorDataService
        .getProcessedSensorData(this.sensorType)
        .subscribe((geoJson: any) => {
          const heatData = geoJson.features.map((feature: any) => {
            const coordinates = feature.geometry.coordinates;
            const value = feature.properties.value;
            return [coordinates[1], coordinates[0], value] as [number, number, number];
          });

          this.cachedData.set(this.sensorType, heatData);
          this.updateGrid();
        });
    }
  }

  private updateGrid(): void {
    if (this.layerGroup) {
      this.layerGroup.clearLayers(); // Remove existing layers if present
    }
    const heatData = this.cachedData.get(this.sensorType);
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
}
