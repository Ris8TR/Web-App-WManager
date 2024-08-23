import { Component } from '@angular/core';
import * as L from 'leaflet';
import { SensorDto } from '../../../../model/sensorDto';
import { HttpClient } from '@angular/common/http';
import { SensorDataService } from '../../../../service/sensorData.service';
import {SensorDataDto} from "../../../../model/sensorDataDto";

@Component({
  selector: 'app-webcam',
  standalone: true,
  imports: [],
  templateUrl: './webcam.component.html',
  styleUrl: './webcam.component.css'
})
export class WebcamComponent {
  constructor(private http: HttpClient, private sensorDataService: SensorDataService) {}

  private map!: L.Map;
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
    this.map = L.map('map').setView([41.8719, 12.5674], 5);
    this.map.setMaxZoom(9); // Imposta il livello di zoom massimo a 9
    //this.map.setMinZoom(5); // Imposta il livello di zoom minimo a 5

    const tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    });
    tileLayer.addTo(this.map);

    this.markerClusterGroup = L.markerClusterGroup({
      spiderfyOnMaxZoom: true,
      showCoverageOnHover: false,
      maxClusterRadius: 40,
      iconCreateFunction: this.createClusterIcon //crea le icone raggruppate
    });
    this.markerClusterGroup.addTo(this.map);
    this.map.on('zoomend', this.onZoomEnd); //Triggher dello zoom
  }

  private loadSensorData(): void {
    this.sensorDataService.getAllSensorBy10MinByType("image").subscribe(
      (sensorDataDtos: SensorDataDto[]) => {
        const markers: L.Marker[] = [];
        console.log(sensorDataDtos);

        sensorDataDtos.forEach((sensorDto: SensorDataDto) => {
          const latitude = sensorDto.latitude!;
          const longitude = sensorDto.longitude!;
          const image = "http://192.168.15.34:8010/v1/images/"+ sensorDto.userId + "/" +sensorDto.payload;

          // Correcting the template string
          const key = `<span class="math-inline">{${latitude}},${longitude}</span><img src="${image}">`;

          // Correcting the sensorCountPerMarker logic
          const sensorCount = this.sensorCountPerMarker[key] || 1;
          this.sensorCountPerMarker[key] = sensorCount + 1;

          const popupContent = `
            Nome: ${sensorDto.id} <br>
            Lat: ${latitude} <br>
            Lon: ${longitude} <br>
          <img src="${image}" style="max-width:80%;" onerror="this.style.display='none';">`;

          const marker = L.marker([latitude, longitude], {
            icon: this.createGreenMarkerIcon(sensorCount) // Create icon
          }).bindPopup(popupContent);

          markers.push(marker);
        });

        this.markerClusterGroup.addLayers(markers);
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
