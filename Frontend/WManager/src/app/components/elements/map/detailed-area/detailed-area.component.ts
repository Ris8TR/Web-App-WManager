import { Component, Input, input } from '@angular/core';
import L from 'leaflet';

@Component({
  selector: 'app-detailed-area',
  standalone: true,
  imports: [],
  templateUrl: './detailed-area.component.html',
  styleUrl: './detailed-area.component.css'
})
export class DetailedAreaComponent {
  private map!: L.Map;
  @Input() interestAreaId: any;

  
  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
  
    }, 10);
  }

  private initMap(): void {
    this.map = L.map('map').setView([41.8719, 12.5674], 5);
    const tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    });
    tileLayer.addTo(this.map);
  }

  
}
