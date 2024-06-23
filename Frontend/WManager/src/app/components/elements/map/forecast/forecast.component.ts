import { Component } from '@angular/core';
import * as L from 'leaflet';
import {SensorDataService} from "../../../../service/sensorData.service";
import {SensorDataDto} from "../../../../model/sensorDataDto";
import {FormsModule} from "@angular/forms";
import {Layer} from "leaflet";
import 'leaflet.heat';
import {MatSelectModule} from "@angular/material/select";
import {MatFormFieldModule} from "@angular/material/form-field";



@Component({
  selector: 'app-forecast',
  standalone: true,
  imports: [FormsModule,MatFormFieldModule,MatSelectModule],
  templateUrl: './forecast.component.html',
  styleUrl: './forecast.component.css'
})
export class ForecastComponent {


  private map: L.Map | undefined;
  public sensorType: string = 'json'; // Default sensor type
  private layerGroup: L.LayerGroup | undefined;

  constructor(private sensorDataService: SensorDataService) {
  }

  ngOnInit(): void {
    this.map = L.map('map').setView([45.0, 7.0], 6); // Set your default coordinates and zoom level
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
    }).addTo(this.map);

    this.layerGroup = L.layerGroup().addTo(this.map);
    this.loadSensorData();
    setInterval(() => this.loadSensorData(), 600000); // Aggiorna ogni 10 minuti
  }

  onSensorTypeChange(): void {
    console.log("Sensor type changed to: ", this.sensorType);
    this.loadSensorData();
  }

  private loadSensorData(): void {
    if (this.layerGroup) {
      this.layerGroup.clearLayers();
    }
    console.log("dsd")
    this.sensorDataService
      .getAllSensorBy10MinByType(this.sensorType)
      .subscribe((data: SensorDataDto[]) => {
        const heatData = data.map((d) => {
          console.log(data)
          const value = this.getSensorValue(d);
          console.log(value)
          return [d.latitude, d.longitude, value] as [number, number, number];
        });
        let heatLayer: Layer;
        heatLayer = L.heatLayer(heatData, {
          radius: 25,
          blur: 15,
          gradient: {0.4: 'blue', 0.65: 'lime', 1: 'red'},
        });
        this.layerGroup?.addLayer(heatLayer);
      });
  }


  private getSensorValue(sensorData: SensorDataDto): number {
    if (sensorData.payload) {
      switch (this.sensorType) {
        case 'CO2':
          return sensorData.payload['CO2'] ?? 0; // CO2 value
        case 'Temperatura':
          return sensorData.payload['temperature'] ?? 0; // Temperature value
        case 'Pressione':
          return sensorData.payload['ap'] ?? 0; // Pressure value (assuming 'ap' is the correct property name)
        case 'Umidita':
          return sensorData.payload['humidity'] ?? 0; // Humidity value (assuming 'humidity' is the correct property name)
        default:
          return 0; // Default value for unknown sensor type
      }
    }
    return 0; // Default value if payload is undefined or null
  }

}

