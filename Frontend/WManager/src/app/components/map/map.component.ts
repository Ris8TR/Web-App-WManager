import { AfterViewInit, Component } from '@angular/core';
import * as L from 'leaflet';
import { ToolbarComponent } from '../toolbar/toolbar.component';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [ToolbarComponent,HttpClientModule ],
  templateUrl: './map.component.html',
  styleUrl: './map.component.css'
})
export class MapComponent implements AfterViewInit {
  constructor() { }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
    }, 10);
  }

  private initMap(): void {
    const map = L.map('map').setView([41.8719, 12.5674], 6); // Coordinate centrali dell'Italia e zoom iniziale
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);
    console.log("utg")
    // Aggiungere eventuali marker o layer sulla mappa qui
  }
}