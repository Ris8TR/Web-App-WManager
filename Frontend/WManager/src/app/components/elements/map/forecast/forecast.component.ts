import { Component, OnInit } from '@angular/core';
import * as L from 'leaflet';
import { SensorDataService } from '../../../../service/sensorData.service';
import { SensorDataDto } from '../../../../model/sensorDataDto';
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
export class ForecastComponent implements OnInit {

  private map: L.Map | undefined;
  public sensorType: string = 'CO2'; // Default sensor type
  private layerGroup: L.LayerGroup | undefined;

  constructor(private sensorDataService: SensorDataService) {}

  ngOnInit(): void {
    this.initializeMap();
    this.layerGroup = L.layerGroup().addTo(this.map!);
    this.loadSensorData();
    setInterval(() => this.loadSensorData(), 600000); // Update every 10 minutes
  }

  private initializeMap(): void {
    this.map = L.map('map').setView([45.0, 7.0], 6); // Set your default coordinates and zoom level
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
    }).addTo(this.map);
  }

  onSensorTypeChange(): void {
    console.log('Sensor type changed to: ', this.sensorType);
    this.loadSensorData();
  }

  /*
  private loadSensorData(): void {
    if (this.layerGroup) {
      this.layerGroup.clearLayers(); // Remove existing layers if present
    }
    this.sensorDataService
      .getAllSensorBy10MinByType('json')
      .subscribe((data: SensorDataDto[]) => {
        const heatData = data.map((d) => {
          const value = this.getSensorValue(d);
          return [d.latitude, d.longitude, value] as [number, number, number];
        });

        const heatLayer = (L as any).heatLayer(heatData, {
          radius: 25,
          blur: 15,
          gradient: { 0.4: 'blue', 0.65: 'lime', 1: 'red' },
          willReadFrequently: true // Optimization attribute
        });

        this.layerGroup?.addLayer(heatLayer); // Add heat layer to the map
      });
  }

   */
  private loadSensorData(): void {
    if (this.layerGroup) {
      this.layerGroup.clearLayers(); // Remove existing layers if present
    }
    this.sensorDataService
      .getProcessedSensorData(this.sensorType)
      .subscribe((geoJson: any) => {
        const heatData = geoJson.features.map((feature: any) => {
          const coordinates = feature.geometry.coordinates;
          const value = feature.properties.value;
          return [coordinates[1], coordinates[0], value] as [number, number, number];
        });

        const heatLayer = (L as any).heatLayer(heatData, {
          radius: 25,
          blur: 15,
          gradient: { 0.4: 'blue', 0.65: 'lime', 1: 'red' },
          willReadFrequently: true // Optimization attribute
        });

        this.layerGroup?.addLayer(heatLayer); // Add heat layer to the map
      });
  }

  private getSensorValue(sensorData: SensorDataDto): number {
    if (sensorData.payload) {
      const payload: { [key: string]: any } = sensorData.payload;

      switch (this.sensorType) {
        case 'CO2':
          return payload['CO2'] ?? 0;
        case 'temperature':
          return payload['temperature'] ?? 0;
        case 'Pression':
          return payload['ap'] ?? 0;
        case 'humidity':
          return payload['humidity'] ?? 0;
        default:
          return 0;
      }
    }
    return 0;
  }
}
