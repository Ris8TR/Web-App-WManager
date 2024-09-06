import {AfterViewInit, Component, OnDestroy, OnInit} from '@angular/core';
import * as L from 'leaflet';
import {SensorDataService} from "../../../../service/sensorData.service";
import {NgIf} from "@angular/common";
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";
import {SensorDto} from "../../../../model/sensorDto";
import {SensorService} from "../../../../service/sensor.service";
import {CookieService} from "ngx-cookie-service";
import {DateDto} from "../../../../model/dateDto";
import {FormsModule} from "@angular/forms";


@Component({
  selector: 'app-interest-area-viewer',
  standalone: true,
  imports: [
    NgIf,
    ToolbarComponent,
    FormsModule
  ],
  templateUrl: './interest-area-viewer.component.html',
  styleUrl: './interest-area-viewer.component.css'
})
export class InterestAreaViewerComponent  implements AfterViewInit, OnDestroy {

  private map: L.Map | undefined;
  private layerGroup: L.LayerGroup | undefined;
  public sensorType: string = 'CO2';  // Default sensor type
  public sensors: SensorDto[] = [];
  public logStringResult: string = 'Login';
  public startDate: string | undefined;
  public endDate: string | undefined;
  public startHour: string | undefined;
  public endHour: string | undefined;
  private cachedData: Map<string, any> = new Map();  // Cache for sensor data

  constructor(
    private sensorDataService: SensorDataService,
    private sensorService: SensorService,
    private cookieService: CookieService,
    public toolbarComponent: ToolbarComponent
  ) {}



  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initializeMap();
      this.layerGroup = L.layerGroup().addTo(this.map!);
      this.loadSensorData();
      this.loadSensors();
      this.logStringResult = this.toolbarComponent.logStringResult;
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

  private loadSensors(): void {
    this.sensorService.findByUserId(this.cookieService.get('token')).subscribe((sensors) => {
      this.sensors = sensors;
    });
  }

  onSensorSelect(sensor: SensorDto): void {
    if (sensor.type != null) {
      this.sensorType = sensor.type;
    }
    this.loadSensorData();
  }

  private loadSensorData(): void {
    if (!this.map) {
      console.error('Map is not initialized yet.');
      return; // Ensure map is initialized before proceeding
    }

    if (this.cachedData.has(this.sensorType!)) {
      this.updateGrid();
    } else {
      this.sensorDataService
        .getProcessedSensorData(this.sensorType!)
        .subscribe((geoJson: any) => {
          const heatData = geoJson.features.map((feature: any) => {
            const coordinates = feature.geometry.coordinates;
            const value = feature.properties.value;
            return [coordinates[1], coordinates[0], value] as [number, number, number];
          });

          this.cachedData.set(this.sensorType!, heatData);
          this.updateGrid();
        });
    }
  }

  onIntervalSelect(event: Event): void {
    const selectedValue = (event.target as HTMLSelectElement).value;
    const interval = parseInt(selectedValue, 10);  // Fix parsing issue by using base 10

    let observable;
    switch (interval) {
      case 5:
        this.sensors = [];
        observable = this.sensorDataService.getAllSensorDataIn5Min();
        break;
      case 10:
        this.sensors = [];
        observable = this.sensorDataService.getAllSensorDataIn10Min();
        break;
      case 15:
        this.sensors = [];
        observable = this.sensorDataService.getAllSensorDataIn15Min();
        break;
      default:
        return;
    }
    console.log(observable)
    observable.subscribe((data) => {
      this.cachedData.set(this.sensorType, this.processData(data));
      this.updateGrid();
    });
  }

  onDateRangeSubmit(): void {
    const formattedStartHour = this.startHour ? this.startHour.padStart(2, '0') : '00';
    const formattedEndHour = this.endHour ? this.endHour.padStart(2, '0') : '00';

    const dateDto: DateDto = {
      form: `${this.startDate}T${formattedStartHour}:00:00`,
      to: `${this.endDate}T${formattedEndHour}:00:00`,
      sensorId: this.sensorType
    };

    console.log(dateDto)

    this.sensorDataService.getAllSensorDataBetweenDate(dateDto).subscribe((data) => {
      this.cachedData.set(this.sensorType, this.processData(data));
      this.updateGrid();
    });
  }


  private addPointsToMap(heatData: [number, number, number][]): void {
    heatData.forEach(dataPoint => {
      const [lat, lng, value] = dataPoint;
      if (lat != null && lng != null && value != null) {  // Check for valid data
        const marker = L.circleMarker([lat, lng], {
          radius: 8,
          fillColor: this.getColorScale()(value),
          color: '#000',
          weight: 1,
          opacity: 1,
          fillOpacity: 0.8
        }).addTo(this.layerGroup!);
        marker.bindPopup('Value: ${value}');
      } else {
        console.error('Invalid data point: ${dataPoint}');
      }
    });
  }

  private updateGrid(): void {
    if (this.layerGroup) {
      this.map!.removeLayer(this.layerGroup);  // Remove the existing layer group
    }
    this.layerGroup = L.layerGroup().addTo(this.map!);  // Recreate the layer group
    const heatData = this.cachedData.get(this.sensorType);
    if (heatData) {
      this.addPointsToMap(heatData);
    }
  }


  private getColorScale() {
    return (value: number) => {
      const min = 0, max = 100;  // Adjust these values based on your data range
      const ratio = (value - min) / (max - min);
      const r = Math.round(255 * ratio);
      const g = 0;
      const b = Math.round(255 * (1 - ratio));
      return 'rgb(${r},${g},${b})';
    };
  }

  private processData(data: any): [number, number, number][] {
    if (!this.map) {
      console.error('Map is not initialized yet.');
      return []; // Early return or handle the case where the map is not initialized
    }
    console.log("Processing data: ", data);
    return data.map((sensorData: any) => {
      const lat = sensorData.latitude;
      const lng = sensorData.longitude;
      const value = sensorData.value;
      console.log('Processed point: lat=${lat}, lng=${lng}, value=${value}');
      return [lat, lng, value];
    });
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }

}
