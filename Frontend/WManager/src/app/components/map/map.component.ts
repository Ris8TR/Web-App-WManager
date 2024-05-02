import { AfterViewInit, Component } from '@angular/core';
import * as L from 'leaflet';
import { ToolbarComponent } from '../toolbar/toolbar.component';
import { HttpClient, HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [ToolbarComponent,HttpClientModule ],
  templateUrl: './map.component.html',
  styleUrl: './map.component.css'
})
export class MapComponent implements AfterViewInit {

  constructor(private http: HttpClient) {}
  private map! : L.Map;
  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
      this.addIcons();
      this.addRainLayer();
    }, 10);
  }

  private initMap(): void {
    this.map = L.map('map').setView([41.8719, 12.5674], 6);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);
  }

// Esempio di aggiunta di un'icona
  private addIcons(): void {
    L.marker([41.8915, 12.46114]).addTo(this.map)
      .bindPopup('Il papa (più o meno)');
    // Aggiungi altre icone come necessario
  }

 // Esempio di aggiunta di un layer semi trasparente per la pioggia. Poi vanno inseriti i servizi da swagger questo è solo per prova (e comunque non funziona )
  private addRainLayer(): void {
    this.http.get('url_del_tuo_file_json').subscribe((data: any) => {
      L.geoJSON(data, {
        style: function (feature) {
          return {
            fillColor: 'blue',
            fillOpacity: 0.5,
            weight: 1,
            color: 'blue',
            opacity: 1
          };
        }
      }).addTo(this.map);
    });
  }
}