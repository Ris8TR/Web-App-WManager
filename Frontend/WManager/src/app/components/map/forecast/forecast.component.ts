import { Component } from '@angular/core';
import * as L from 'leaflet';

@Component({
  selector: 'app-forecast',
  standalone: true,
  imports: [],
  templateUrl: './forecast.component.html',
  styleUrl: './forecast.component.css'
})
export class ForecastComponent {

  private map!: L.Map;

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
  
    }, 10);
  }

  private initMap(): void {
    this.map = L.map('map').setView([41.8719, 12.5674], 6);
    const tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    });
    tileLayer.addTo(this.map);
  }

}
