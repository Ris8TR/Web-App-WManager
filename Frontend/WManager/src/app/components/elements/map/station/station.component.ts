import {AfterViewInit, Component, OnDestroy} from '@angular/core';
import * as L from 'leaflet';
import { HttpClient } from '@angular/common/http';
import { ToolbarComponent } from '../../toolbar/toolbar.component';

import 'leaflet.markercluster';
import { SensorDto } from '../../../../model/sensorDto';
import { UserService } from '../../../../service/user.service';

@Component({
  selector: 'app-obsmap',
  standalone: true,
  imports: [ToolbarComponent,
// TODO: `HttpClientModule` should not be imported into a component directly.
// Please refactor the code to add `provideHttpClient()` call to the provider list in the
// application bootstrap logic and remove the `HttpClientModule` import from this component.
],
  templateUrl: './station.component.html',
  styleUrls: ['./station.component.css']
})
export class StationComponent implements AfterViewInit, OnDestroy {
  constructor(private http: HttpClient, private userService: UserService) {}

  private obsmap!: L.Map;
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
    if (this.obsmap) {
      this.obsmap.remove();
    }
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
    this.obsmap = L.map('map').setView([41.8719, 12.5674], 5);
    this.obsmap.setMaxZoom(13); // Imposta il livello di zoom massimo a 9
    this.obsmap.setMinZoom(5); // Imposta il livello di zoom minimo a 5

    const tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    });
    tileLayer.addTo(this.obsmap);

    this.markerClusterGroup = L.markerClusterGroup({
      spiderfyOnMaxZoom: true,
      showCoverageOnHover: false,
      maxClusterRadius: 40,
      iconCreateFunction: this.createClusterIcon //crea le icone raggruppate
    });
    this.markerClusterGroup.addTo(this.obsmap);
    this.obsmap.on('zoomend', this.onZoomEnd); //Triggher dello zoom
  }

  private loadSensorData(): void {
    this.userService.getAllSensor().subscribe(
      (sensorDtos: SensorDto[]) => {
        const markers: L.Marker[] = [];
        console.log(sensorDtos)
        if (sensorDtos) {
          sensorDtos.forEach((sensorDto: SensorDto) => {
            const latitude = sensorDto.latitude!;
            const longitude = sensorDto.longitude!;
            const key = `${latitude},${longitude}`; // Usa template literals per creare la chiave unica

            // Incrementa il contatore dei sensori per la chiave
            this.sensorCountPerMarker[key] = (this.sensorCountPerMarker[key] || 0) + 1;
            const sensorCount = this.sensorCountPerMarker[key];

            const popupContent = `Nome: ${sensorDto.id} <br>Lat: ${latitude} <br>Lon: ${longitude}`;
            const marker = L.marker([latitude[0], longitude[0]], {
              icon: this.createGreenMarkerIcon(sensorCount) // Crea icone in base al conteggio
            })
              .bindPopup(popupContent);

            markers.push(marker);
          });

          // Aggiungi i markers al marker cluster group
          this.markerClusterGroup.clearLayers(); // Pulisce i markers esistenti
          this.markerClusterGroup.addLayers(markers);
        }
      },
      (error: any) => {
        console.error('Error loading sensor data:', error);
      }
    );
  }

  private onZoomEnd = () => {
    if (this.markerClusterGroup) {
      this.markerClusterGroup.refreshClusters();
    }
  };

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
      this.loadSensorData();
    }, 10);
  }
}
