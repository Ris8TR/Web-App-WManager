import {AfterViewInit, Component, OnDestroy, OnInit} from '@angular/core';
import * as L from 'leaflet';
import {SensorDataService} from "../../../../service/sensorData.service";
import {APP_BASE_HREF, LocationStrategy, NgClass, NgForOf, NgIf, PathLocationStrategy} from "@angular/common";
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";
import {SensorDto} from "../../../../model/sensorDto";
import {SensorService} from "../../../../service/sensor.service";
import {CookieService} from "ngx-cookie-service";
import {DateDto} from "../../../../model/dateDto";
import {FormsModule} from "@angular/forms";
import {ActivatedRoute, ParamMap} from "@angular/router";


@Component({
  selector: 'app-interest-area-viewer',
  standalone: true,
  imports: [
    NgIf,
    ToolbarComponent,
    FormsModule,
    NgForOf,
    NgClass
  ],
  providers: [
    { provide: LocationStrategy, useClass: PathLocationStrategy },
    { provide: APP_BASE_HREF, useValue: '/' } // Imposta il base href
  ],
  templateUrl: './interest-area-viewer.component.html',
  styleUrl: './interest-area-viewer.component.css'
})
export class InterestAreaViewerComponent implements AfterViewInit, OnDestroy, OnInit {

  private map: L.Map | undefined;
  id: string | null = null;
  selectedSensor!: string | undefined;
  private layerGroup: L.LayerGroup | undefined;
  public sensorType: string = 'CO2';
  public sensors: SensorDto[] = [];
  public logStringResult: string = 'Login';
  public startDate?: string;
  public endDate?: string;
  public startHour?: string;
  public endHour?: string;
  private cachedData: Map<string, [number, number, number][]> = new Map();

  constructor(
    private sensorDataService: SensorDataService,
    private sensorService: SensorService,
    private cookieService: CookieService,
    public toolbarComponent: ToolbarComponent,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.id = params.get('id');
      this.reloadComponentData();
    });
    }

  ngAfterViewInit(): void {
    setTimeout(() => {
      if (!this.map) this.initializeMap();
      if (!this.layerGroup) this.layerGroup = L.layerGroup().addTo(this.map!);
      this.reloadComponentData();
      this.logStringResult = this.toolbarComponent.logStringResult;
    }, 10);
  }

  ngOnDestroy(): void {
    if (this.map) this.map.remove();
  }



  private reloadComponentData(): void {
    if (!this.id) return;
    this.cachedData.clear();
    this.loadSensors();
    this.loadSensorData();
  }

  private initializeMap(): void {
    this.map = L.map('map').setView([45.0, 7.0], 5);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 8,
      minZoom: 5
    }).addTo(this.map);

    this.map.on('moveend', () => this.updateGrid());
  }

  private loadSensors(): void {
    this.sensorService.findByInterestAreaId(this.id!, this.cookieService.get('token'))
      .subscribe(sensors => this.sensors = sensors);
    if (this.sensors.length>0){
      this.selectedSensor=this.sensors[0].id
    }
  }

  onSensorSelect(sensor: SensorDto): void {
    if (sensor.type) this.sensorType = sensor.type;
    this.loadSingleSensorData(sensor);
    this.selectedSensor = sensor.id
    this.loadSensorData();
    return
  }

  private loadSingleSensorData(sensor: SensorDto): void {
    this.sensorDataService.getSensorDataBySensorId(sensor.id!, this.cookieService.get('token'))
      .subscribe(sensorData => {
        if (sensorData?.latitude && sensorData.longitude && this.map) {
          this.map.setView([sensorData.latitude, sensorData.longitude], 10);
        }
      });
  }

  private loadSensorData(): void {
    if (!this.map) return;

    const cachedData = this.cachedData.get(this.sensorType);
    if (cachedData) {
      this.updateGrid();
    } else {
      this.sensorDataService.getProcessedSensorData(this.sensorType)
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

            this.cachedData.set(this.sensorType, heatData);
            this.updateGrid();
          } else {
            console.error('Formato della risposta non valido:', geoJson);
          }
        });
    }
  }


  onIntervalSelect(event: Event): void {
    const interval = parseInt((event.target as HTMLSelectElement).value, 10);
    const intervalObservable = this.getIntervalObservable(interval);

    if (intervalObservable) {
      intervalObservable.subscribe(data => {
        this.cachedData.set(this.sensorType, this.processData(data));
        this.updateGrid();
      });
    }
  }

  private getIntervalObservable(interval: number) {
    switch (interval) {
      case 5: return this.sensorDataService.getAllSensorDataIn5Min();
      case 10: return this.sensorDataService.getAllSensorDataIn10Min();
      case 15: return this.sensorDataService.getAllSensorDataIn15Min();
      default: return null;
    }
  }


  onDateRangeSubmit(): void {
    const now = new Date();
    const formattedStartHour = this.startHour || now.getHours().toString().padStart(2, '0');
    const formattedEndHour = this.endHour || now.getHours().toString().padStart(2, '0');

    const defaultDate = now.toISOString().split('T')[0];  // "yyyy-MM-dd" corrente
    console.log(this.selectedSensor)

    if (this.selectedSensor != null) {
      const dateDto: DateDto = {
        form: `${this.startDate || defaultDate}T${formattedStartHour}:${now.getMinutes().toString().padStart(2, '0')}:00`,
        to: `${this.endDate || defaultDate}T${formattedEndHour}:${now.getMinutes().toString().padStart(2, '0')}:00`,
        sensorId: this.selectedSensor

      };
      console.log(dateDto);


      this.sensorDataService.getAllSensorDataBetweenDate(dateDto).subscribe(data => {
        this.cachedData.set(this.sensorType, this.processData(data));
        console.log(data)
        this.updateGrid();
      });
    }
  }



  private updateGrid(): void {
    if (this.layerGroup) this.map!.removeLayer(this.layerGroup);
    this.layerGroup = L.layerGroup().addTo(this.map!);
    this.addPointsToMap(this.cachedData.get(this.sensorType) || []);
  }

  private addPointsToMap(heatData: [number, number, number][]): void {
    heatData.forEach(([lat, lng, value]) => {
      if (lat != null && lng != null && value != null) {
        L.circleMarker([lat, lng], {
          radius: 8,
          fillColor: this.getColor(value),
          color: '#000',
          weight: 1,
          opacity: 1,
          fillOpacity: 0.8
        }).bindPopup(`Value: ${value}`).addTo(this.layerGroup!);
      }
    });
  }

  private getColor(value: number): string {
    const min = 0, max = 100;
    const ratio = (value - min) / (max - min);
    const r = Math.round(255 * ratio);
    const b = Math.round(255 * (1 - ratio));
    return `rgb(${r},0,${b})`;
  }

  private processData(data: any): [number, number, number][] {
    console.log("Processing data: ", data);
    return data.map((sensorData: any) => {
      const lat = sensorData.latitude;
      const lng = sensorData.longitude;
      let value: number | undefined;

      // Parsing il campo payload per ottenere i dati specifici del sensore
      try {
        const payloadData = JSON.parse(sensorData.payload);
        value = payloadData[this.sensorType];
      } catch (error) {
        console.error("Errore nel parsing del payload:", error);
      }

      console.log(`Processed point: lat=${lat}, lng=${lng}, value=${value}`);
      return [lat, lng, value || 0]; // Default a 0 se value è undefined
    });
  }



}
