import {AfterViewInit, Component, Input, OnDestroy, OnInit} from '@angular/core';
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
import {InterestAreaDto} from "../../../../model/interestAreaDto";
import {InterestAreaService} from "../../../../service/interestArea.service";
import {InterestArea} from "../../../../model/interestArea";
import { parse } from 'terraformer-wkt-parser';
import {geometry} from "@turf/turf";
import {Subscription} from "rxjs";
import {MatSnackBar} from "@angular/material/snack-bar";


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
  selectedSensor!: string | undefined;
  interestArea: InterestArea | undefined;
  id: string | undefined | null;
  private layerGroup: L.LayerGroup | undefined;
  public sensorType: string = 'CO2';
  public sensors: SensorDto[] = [];
  public logStringResult: string = 'Login';
  public startDate?: string;
  public endDate?: string;
  public startHour?: string;
  public endHour?: string;
  private cachedData: Map<string, [number, number, number][]> = new Map();
  private drawnLayers: L.Layer[] = [];
  private subscription: Subscription = new Subscription();
  isPanelVisible = true;

  temperatureScale = [
    { label: '-10', color: '#0030ff' },
    { label: '-8', color: '#0066ff' },
    { label: '-6', color: '#00a4ff' },
    { label: '-4', color: '#00d7ff' },
    { label: '-2', color: '#00f9ed' },
    { label: '0', color: '#00ebbd' },
    { label: '2', color: '#00dc8d' },
    { label: '4', color: '#00c951' },
    { label: '6', color: '#01ba1c' },
    { label: '8', color: '#21bd05' },
    { label: '10', color: '#61cf03' },
    { label: '12', color: '#93df01' },
    { label: '14', color: '#cff000' },
    { label: '16', color: '#ffff00' },
    { label: '18', color: '#ffed00' },
    { label: '20', color: '#ffd700' },
    { label: '22', color: '#ffc400' },
    { label: '24', color: '#ffaf00' },
    { label: '26', color: '#ff9200' },
    { label: '28', color: '#ff7100' },
    { label: '30', color: '#ff4700' },
    { label: '32', color: '#ff2300' },
    { label: '34', color: '#ff0100' },
    { label: '36', color: '#de0014' },
    { label: '38', color: '#bd0033' },
    { label: '40', color: '#940056' },
    { label: '42', color: '#730073' }
  ];


  constructor(
    private sensorDataService: SensorDataService,
    private interestAreaService: InterestAreaService,
    private sensorService: SensorService,
    private snackBar: MatSnackBar,
    private cookieService: CookieService,
    public toolbarComponent: ToolbarComponent,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.subscription = this.interestAreaService.currentId$.subscribe(id => {
      this.id = id;
      this.reloadComponent(); // Chiama un metodo di ricarica o aggiorna la visualizzazione
      this.reloadComponentData();
    })

    if (this.id == null){
      this.snackBar.open("Selezionare un'area di interesse" , "ok")
      return
    }

    this.interestAreaService.getInterestArea(this.id!, this.cookieService.get('token')).subscribe(area => {
      this.interestArea = area;
      console.log(this.interestArea);

      // Disegna l'area d'interesse sulla mappa
      if (this.interestArea && this.interestArea.geometry) {
        this.drawInterestArea(this.interestArea.geometry);
      }
    });
  }

  reloadComponent() {
    console.log(`Caricamento del componente con ID: ${this.id}`);
    // Qui puoi aggiungere logica per aggiornare la visualizzazione se necessario
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
    this.subscription.unsubscribe();

  }

  togglePanel(event: MouseEvent): void {
    event.stopPropagation(); // Evita che il click sul pannello tocchi elementi sottostanti
    this.isPanelVisible = !this.isPanelVisible; // Alterna la visibilità del pannello
  }


  private drawInterestArea(geometry: string): void {
    try {
      // Rimuove eventuali punti e virgola finali
      geometry = geometry.trim().replace(/;$/, '');

      // Parse del WKT in GeoJSON
      const geoJson = parse(geometry);

      // Rimuove le geometrie già disegnate
      this.removeDrawnAreas();

      // Verifica il tipo di geometria e gestisce separatamente Polygon, MultiPolygon e LineString
      if (geoJson && (geoJson.type === 'Polygon' || geoJson.type === 'MultiPolygon')) {
        const polygon = L.geoJSON(geoJson, {
          style: {
            color: 'blue',    // Colore dei bordi del poligono
            weight: 4,        // Spessore dei bordi
            opacity: 0.7      // Opacità dei bordi
          }
        }).addTo(this.map!);

        // Aggiungi il poligono all'array dei layer disegnati
        this.drawnLayers.push(polygon);

        // Centra la vista sul poligono
        this.map!.fitBounds(polygon.getBounds());

      } else if (geoJson && (geoJson.type === 'LineString' || geoJson.type === 'MultiLineString')) {
        // @ts-ignore
        const polyline = L.polyline(geoJson.coordinates, {
          color: 'blue',   // Colore della linea
          weight: 4,       // Spessore della linea
          opacity: 0.7     // Opacità della linea
        }).addTo(this.map!);

        // Aggiungi la linea all'array dei layer disegnati
        this.drawnLayers.push(polyline);

        // Centra la vista sulla linea
        this.map!.fitBounds(polyline.getBounds());

      } else {
        console.error('Tipo di geometria non valido o non supportato:', geoJson.type);
      }
    } catch (error) {
      console.error('Errore durante il parsing WKT:', error);
    }
  }

// Metodo per rimuovere le aree già disegnate
  private removeDrawnAreas(): void {
    if (this.drawnLayers.length > 0) {
      this.drawnLayers.forEach(layer => {
        this.map!.removeLayer(layer); // Rimuovi il layer dalla mappa
      });
      this.drawnLayers = []; // Svuota l'array
    }
  }


  private reloadComponentData(): void {
    if (!this.id) return;
    this.cachedData.clear();
    this.interestAreaService.getInterestArea(this.id!, this.cookieService.get('token')).subscribe(area => {
      this.interestArea = area;
      console.log(this.interestArea);
    });
    this.loadSensors();
    this.loadSensorData();
    if (this.interestArea != null) {
      this.drawInterestArea(this.interestArea!.geometry);
    }
  }

  private initializeMap(): void {
    this.map = L.map('map').setView([45.0, 7.0], 5);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 14,
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


      this.sensorDataService.getAllSensorDataBySensorBetweenDate(dateDto).subscribe(data => {
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
