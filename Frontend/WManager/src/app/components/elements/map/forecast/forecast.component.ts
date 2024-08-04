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
      this.createGrid(heatData);
    }
  }

  private createGrid(heatData: [number, number, number][]): void {
    const bounds = this.map!.getBounds();
    const cellSize = 0.5; // Increased cell size to reduce the number of cells
    const colorScale = this.getColorScale();

    for (let lat = bounds.getSouth(); lat < bounds.getNorth(); lat += cellSize) {
      for (let lng = bounds.getWest(); lng < bounds.getEast(); lng += cellSize) {
        const cellValue = this.getAverageValueInCell(lat, lng, cellSize, heatData);
        const color = colorScale(cellValue);

        const rectangle = L.rectangle(
          [[lat, lng], [lat + cellSize, lng + cellSize]],
          { color: color, weight: 0, fillOpacity: 0.7 }
        );
        this.layerGroup!.addLayer(rectangle);
      }
    }
  }

  private getAverageValueInCell(lat: number, lng: number, cellSize: number, heatData: [number, number, number][]): number {
    let totalValue = 0;
    let count = 0;

    heatData.forEach(dataPoint => {
      const [pointLat, pointLng, value] = dataPoint;
      if (pointLat >= lat && pointLat < lat + cellSize && pointLng >= lng && pointLng < lng + cellSize) {
        totalValue += value;
        count++;
      }
    });

    return count > 0 ? totalValue / count : 0;
  }

  private getColorScale() {
    return (value: number) => {
      // Implement a color scale based on the value, e.g., using d3-scale
      // For simplicity, here is a simple linear color scale from blue to red
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
