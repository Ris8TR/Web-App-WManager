import {AfterViewInit, Component, OnDestroy, OnInit} from '@angular/core';
import * as L from 'leaflet';
import {SensorDataService} from "../../../../service/sensorData.service";
import {NgForOf, NgIf} from "@angular/common";
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";
import {SensorDto} from "../../../../model/sensorDto";
import {SensorService} from "../../../../service/sensor.service";
import {CookieService} from "ngx-cookie-service";
import {DateDto} from "../../../../model/dateDto";
import {FormsModule} from "@angular/forms";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {SensorDataDto} from "../../../../model/sensorDataDto";


@Component({
  selector: 'app-interest-area-viewer',
  standalone: true,
  imports: [
    NgIf,
    ToolbarComponent,
    FormsModule,
    NgForOf
  ],
  templateUrl: './interest-area-viewer.component.html',
  styleUrl: './interest-area-viewer.component.css'
})
export class InterestAreaViewerComponent implements AfterViewInit, OnDestroy, OnInit{

  private map: L.Map | undefined;
  id: string | null = null;
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
    public toolbarComponent: ToolbarComponent,
    private route: ActivatedRoute
  ) {}


  ngOnInit(): void {
    // Osserva il cambiamento dell'id e carica i dati quando cambia
    this.route.paramMap.subscribe((params: ParamMap) => {
      const newId = params.get('id');
      if (newId !== this.id) {
        this.id = newId;
        this.reloadComponentData();
      }
    });
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      if (!this.map) {
        this.initializeMap();
      }
      if (!this.layerGroup) {
        this.layerGroup = L.layerGroup().addTo(this.map!);
      }
      this.reloadComponentData();  // Carica i dati inizialmente
      this.logStringResult = this.toolbarComponent.logStringResult;
    }, 10);
  }

  // Funzione che ricarica tutti i dati quando l'id cambia
  private reloadComponentData(): void {
    if (!this.id) {
      return;
    }

    // Ripulisce la cache quando cambia l'id
    this.cachedData.clear();
    // Carica i dati dei sensori e aggiorna la mappa
    this.loadSensors();
    this.loadSensorData();
  }

  private initializeMap(): void {
    if (!this.map) {  // Verifica se la mappa è già stata inizializzata
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
  }

  private loadSensors(): void {
    this.sensorService.findByInterestAreaId(this.id!, this.cookieService.get('token')).subscribe((sensors) => {
      this.sensors = sensors;
    });
    console.log(this.sensors)
    console.log("Sensor " + this.sensors)
    console.log("Sensor cose " + this.id + "   " +  this.cookieService.get('token'))

  }
  onSensorSelect(sensor: SensorDto): void {
    if (sensor.type != null) {
      this.sensorType = sensor.type;
    }

    // Carica i dati relativi al sensore selezionato
    this.sensorDataService.getSensorDataBySensorId(sensor.id!, this.cookieService.get('token')).subscribe((sensorData: SensorDataDto) => {
      if (sensorData ) {
        console.log("Sensor data" + sensorData)
        const latestData = sensorData; // Prendi il dato più recente o il primo disponibile
        if (latestData.latitude && latestData.longitude) {
          const lat = latestData.latitude;  // Assumi che il primo valore sia la latitudine
          const lng = latestData.longitude; // Assumi che il primo valore sia la longitudine

          // Sposta la mappa sulla posizione del sensore selezionato e fai zoom
          if (this.map) {
            this.map.setView([lat, lng], 10);  // Imposta lo zoom a 10 o il livello che preferisci
          }
        } else {
          console.error('Coordinate mancanti per il sensore selezionato');
        }
      } else {
        console.log(sensor)
        console.error('Nessun dato trovato per il sensore selezionato');
      }
    });

    this.loadSensorData();
  }



  private loadSensorData(): void {
    if (!this.map) {
      console.error('Map is not initialized yet.');
      return; // Fermati finché la mappa non è inizializzata
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
        marker.bindPopup(`Value: ${value}`);
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
