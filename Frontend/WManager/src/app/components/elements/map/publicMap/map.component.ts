import {AfterViewInit, Component, OnDestroy, OnInit} from '@angular/core';
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
import {SensorData} from "../../../../model/sensorData";
import {parse} from "terraformer-wkt-parser";
import {InterestAreaService} from "../../../../service/interestArea.service";
import {InterestArea} from "../../../../model/interestArea";
import {InterestAreaDto} from "../../../../model/interestAreaDto";
import {saveOutputToFile} from "source-map-explorer/lib/output";
import {control} from "leaflet";
import zoom = control.zoom;

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
export class MapComponent implements OnInit, AfterViewInit, OnDestroy {
  protected isRealTime = false;

  constructor(private http: HttpClient,     private interestAreaService: InterestAreaService, private userService: UserService, private sensorDataService: SensorDataService) {
  }

  private map!: L.Map;
  sensorOptions = [
    {label: 'Temperature', value: 'temperature', selected: false},
    {label: 'CO2', value: 'CO2', selected: false},
    {label: 'Humidity', value: 'humidity', selected: false},
    {label: 'Pression', value: 'ap', selected: false},
  ];
  public selectedSensorType: string = "CO2";
  selectedLatestInterval: string | null = null;
  selectedForecastInterval: string | null = null;
  sensorTypeList!: string[];
  private drawnLayers: L.Layer[] = [];
  interestAreas: InterestAreaDto[] | undefined;

  private sensorDataLocalList: Array<SensorData> | undefined;


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

  radioOptions = [
    { value: false, label: 'Latest' },
    { value: true, label: 'Real-Time' }

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
    if (this.map) {
      this.map.remove();
    }
  }

  onSensorTypeSelect(type: any) {
    this.selectedSensorType = type;
    const heatData: [number, number, number][] = [];  // Initialize outside the loop

    this.sensorDataLocalList!.forEach((data: any) => {
      const lat = data.latitude;
      const lng = data.longitude;
      let value: number = 0;

      try {
        const payloadData = JSON.parse(data.payload);
        value = payloadData[this.selectedSensorType] || 0; // Usa il tipo selezionato
      } catch (error) {
        console.error("Errore nel parsing del payload:", error);
      }

      if (lat && lng) {
        heatData.push([lat, lng, value]);
      }
    });

    this.cachedData.set(this.selectedSensorType, heatData);

    this.updateGrid();  // Aggiorna la mappa con i nuovi dati
  }


  onSensorTypeChange() {
    let typeElement: HTMLSelectElement | null = document.getElementById('type') as HTMLSelectElement | null;
    this.selectedSensorType = String(typeElement);
    const heatData: [number, number, number][] = [];  // Initialize outside the loop

    this.sensorDataLocalList!.forEach((data: any) => {
      const lat = data.latitude;
      const lng = data.longitude;
      let value: number = 0;

      try {
        const payloadData = JSON.parse(data.payload);
        value = payloadData[this.selectedSensorType] || 0; // Usa il tipo selezionato
      } catch (error) {
        console.error("Errore nel parsing del payload:", error);
      }

      if (lat && lng) {
        heatData.push([lat, lng, value]);
      }
    });

    this.cachedData.set(this.selectedSensorType, heatData);

    this.updateGrid();  // Aggiorna la mappa con i nuovi dati
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
    if (!this.map){
    this.map = L.map('map').setView([41.8719, 12.5674], 5);}
    this.map.setMaxZoom(18);
    this.map.setMinZoom(5);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);

    this.layerGroup = L.layerGroup().addTo(this.map); // Initialize layer group

    this.markerClusterGroup = L.markerClusterGroup({
      spiderfyOnMaxZoom: true,
      showCoverageOnHover: false,
      maxClusterRadius: 40,
      iconCreateFunction: this.createClusterIcon
    }).addTo(this.map);

    this.map.on('zoomend', this.onZoomEnd);
  }




  private updateGrid(): void {
    if (!this.layerGroup) {
      this.layerGroup = L.layerGroup().addTo(this.map);
    }
    this.layerGroup.clearLayers(); // Safely clear existing layers
    const heatData = this.cachedData.get(this.selectedSensorType);
    if (heatData) {
      this.addPointsToMap(heatData);
    }
  }




  private addPointsToMap(heatData: [number, number, number][]): void {
    if (!this.layerGroup) {
      console.error("Layer group is not initialized.");
      return;
    }
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
      marker.bindPopup(this.selectedSensorType + ` value: ${value}`);
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
      this.loadInterestAreas();
      this.loadSensorData();
    }, 15);
  }



  private loadSensorData(): void {
    if (!this.isRealTime) {
      if (!this.map) return;
      this.cachedData.clear()
      this.sensorDataService.getAllPublicSensorDataOnTop()
        .subscribe((response: any) => {
          let geoJson: any;
          const sensorDataList = response.sensorData; // Lista di dati sensori
          const sensorAreaTypes = response.sensorAreaTypes; // Tipi di sensori unici
          this.sensorTypeList = response.sensorAreaTypes;
          this.sensorDataLocalList = response.sensorData;

          if (sensorDataList && sensorAreaTypes) {
            const heatData: [number, number, number][] = [];

            // Estrai le informazioni di geolocalizzazione e valore per ogni sensore
            sensorDataList.forEach((data: any) => {
              // Ottieni la latitudine, longitudine e il valore per il sensore
              const lat = data.latitude;
              const lng = data.longitude;
              let value: number = 0;

              // Aggiungi il valore del sensore in base al tipo selezionato
              try {
                const payloadData = JSON.parse(data.payload);
                value = payloadData[this.selectedSensorType] || 0; // Usa il tipo selezionato
              } catch (error) {
                console.error("Errore nel parsing del payload:", error);
              }

              if (lat && lng) {
                heatData.push([lat, lng, value]);
              }
            });

            // Salva i dati nella cache per il tipo di sensore selezionato
            this.cachedData.set(this.selectedSensorType, heatData);
            this.updateGrid();  // Aggiorna la mappa con i nuovi dati
          } else {
            console.error('Formato della risposta non valido:', response);
          }
        });

    }
  }


  onLatestIntervalSelect(): void {
    this.cachedData.clear();

    let latestElement: HTMLSelectElement | null = document.getElementById('latestInterval') as HTMLSelectElement | null;

    if (latestElement) {
      const selectedInterval = latestElement.value;
      console.log('Selected observation interval:', selectedInterval);

      const interval = parseInt(selectedInterval, 10);

      if (!isNaN(interval)) {
        let intervalObservable = this.getIntervalObservable(interval);

        if (intervalObservable) {
          // @ts-ignore
          intervalObservable.subscribe(
            (response: any) => {
              console.log('Data received from observable:', response);
              if (response) {
                const sensorDataList = response.sensorData;
                const sensorAreaTypes = response.sensorAreaTypes;
                this.sensorTypeList = sensorAreaTypes;
                this.sensorDataLocalList = sensorDataList;

                if (sensorDataList && sensorAreaTypes) {
                  const heatData: [number, number, number][] = [];

                  // Estrai i dati di geolocalizzazione e valore per ogni sensore
                  sensorDataList.forEach((data: any) => {
                    const lat = data.latitude;
                    const lng = data.longitude;
                    let value = 0;

                    try {
                      const payloadData = JSON.parse(data.payload);
                      value = payloadData[this.selectedSensorType] || 0;
                    } catch (error) {
                      console.error("Errore nel parsing del payload:", error);
                    }

                    if (lat && lng) {
                      heatData.push([lat, lng, value]);
                    }
                  });

                  // Salva i dati nella cache per il tipo di sensore selezionato
                  this.cachedData.set(this.selectedSensorType, heatData);
                  this.updateGrid();  // Aggiorna la mappa con i nuovi dati
                } else {
                  console.error('Formato della risposta non valido:', response);
                }
              } else {
                console.warn('No data returned from observable.');
              }
            },
            ( error: any) => {
              console.error('Error during observable subscription:', error);
            }
          );
        } else {
          console.warn('Interval observable is not available for interval:', interval);
        }
      } else {
        console.warn('Invalid or unsupported interval value:', selectedInterval);
      }
    } else {
      console.warn('latestElement is not defined.');
    }
  }

  private getIntervalObservable(interval: number) {
    if (this.isRealTime) {
      switch (interval) {
        case 5:
          console.log("RT 5");
          return this.sensorDataService.getAllPublicSensorDataIn5Min();
        case 10:
          console.log("RT 10");
          return this.sensorDataService.getAllPublicSensorDataIn10Min();
        case 15:
          console.log("RT 15");
          return this.sensorDataService.getAllPublicSensorDataIn15Min();
        default:
          return null;
      }
    } else {
      console.log("L A");
      return this.sensorDataService.getAllPublicSensorDataOnTop();
    }
  }




  ngOnInit(): void {

  }

  private loadInterestAreas() {
    this.interestAreaService.getAllPublicInterestArea().subscribe(areas => {
      this.interestAreas=areas
      if (this.interestAreas) {
        this.drawInterestArea(this.interestAreas);
      }
    });
  }

  private drawInterestArea(areas: InterestAreaDto[]): void {
    try {
      this.removeDrawnAreas();

      areas.forEach(area => {
        let geometry = area.geometry?.trim().replace(/;$/, ''); // Rimuove eventuali punti e virgola finali

        // Parse del WKT in GeoJSON
        const geoJson = parse(geometry!);

        // Verifica il tipo di geometria e gestisce separatamente Polygon, MultiPolygon e LineString
        if (geoJson && (geoJson.type === 'Polygon' || geoJson.type === 'MultiPolygon')) {
          const polygon = L.geoJSON(geoJson, {
            style: {
              color: 'blue',    // Colore dei bordi del poligono
              weight: 4,        // Spessore dei bordi
              opacity: 0.7      // Opacità dei bordi
            }
          })
            .bindPopup(`Area di interesse: ${area.name}`)
            .addTo(this.map!);

          // Aggiungi il poligono all'array dei layer disegnati
          this.drawnLayers.push(polygon);

        } else if (geoJson && (geoJson.type === 'LineString' || geoJson.type === 'MultiLineString')) {
          // @ts-ignore
          const polyline = L.polyline(geoJson.coordinates, {
            color: 'blue',   // Colore della linea
            weight: 4,       // Spessore della linea
            opacity: 0.7     // Opacità della linea
          })
            .bindPopup(`Area di interesse: ${area.name}`)
            .addTo(this.map!);

          // Aggiungi la linea all'array dei layer disegnati
          this.drawnLayers.push(polyline);

        } else {
          console.error('Tipo di geometria non valido o non supportato:', geoJson?.type);
        }
      });

      // Centra la vista su tutte le geometrie disegnate
      if (this.drawnLayers.length > 0) {
        const bounds = L.featureGroup(this.drawnLayers).getBounds();
        this.map!.fitBounds(bounds);
      }
    } catch (error) {
      console.error('Errore durante il parsing delle geometrie:', error);
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
}
