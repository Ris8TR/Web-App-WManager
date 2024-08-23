import { HttpClient } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import L from 'leaflet';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { InterestAreaService } from '../../../../service/interestArea.service';
import { InterestArea } from '../../../../model/interestArea';

@Component({
  selector: 'app-detailed-area',
  standalone: true,
  imports: [],
  templateUrl: './detailed-area.component.html',
  styleUrl: './detailed-area.component.css'
})
export class DetailedAreaComponent implements OnInit {
  private map!: L.Map;
  id: string | null = null;

  constructor(private route: ActivatedRoute, private interestAreaService: InterestAreaService) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params: ParamMap) => {
      this.id = params.get('id');
      if (this.id) {
        this.loadAreaGeometry(this.id);
        console.log(this.id);
      }
    });
  }

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

  private loadAreaGeometry(id: string | undefined): void {
    if (!id) {
      console.error('ID is undefined or null.');
      return;
    }

    this.interestAreaService.getInterestArea(id).subscribe(
      (area: InterestArea) => {
        console.log('Interest Area loaded:', area);
        this.drawInterestArea(area.geometry);
      },
      (error: any) => {
        console.error('Error loading interest area:', error);
      }
    );
  }

  private drawInterestArea(geometry: string): void {
    // Converti la geometria da WKT o GeoJSON in coordinate utilizzabili da Leaflet
    const parsedGeometry = this.parseGeometry(geometry);

    // Disegna il poligono o polilinea in rosso
    if (parsedGeometry) {
      L.polygon(parsedGeometry, { color: 'red' }).addTo(this.map);
    } else {
      console.error('Invalid geometry format');
    }
  }

  private parseGeometry(geometry: string): L.LatLngExpression[] | null {
    try {
      const geoJson = JSON.parse(geometry);
      const coordinates = geoJson.coordinates[0];

      return coordinates.map((coord: number[]) => [coord[1], coord[0]]);
    } catch (error) {
      console.error('Failed to parse geometry:', error);
      return null;
    }
  }
}
