import { AfterViewInit, Component } from '@angular/core';
import * as L from 'leaflet';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { ToolbarComponent } from '../../toolbar/toolbar.component';
import { SensorDto } from '../../../model/sensorDto';
import { UserService } from '../../../service/user.service';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [ToolbarComponent,HttpClientModule ],
  templateUrl: './map.component.html',
  styleUrl: './map.component.css'
})
export class MapComponent implements AfterViewInit {

  constructor(private http: HttpClient, private userService: UserService) {}

  private map!: L.Map;

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
      this.loadSensorData();
    }, 10);
  }

  private initMap(): void {
    this.map = L.map('map').setView([41.8719, 12.5674], 6);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);
  }
  private loadSensorData(): void {
    this.userService.getAllSensor().subscribe(
      (sensorDtos: SensorDto[]) => {
        sensorDtos.forEach((sensorDto: SensorDto) => {
          L.marker([sensorDto.latitude!, sensorDto.longitude!]).addTo(this.map)
            .bindPopup(sensorDto.firstName || String(sensorDto.latitude) || String(sensorDto.longitude) ||'');
        });
      },
      (error: any) => {
        console.error('Error loading sensor data:', error);
      }
    );
  }
  
}
