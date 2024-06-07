import { HttpClient } from '@angular/common/http';
import { Component, Input, OnInit, input } from '@angular/core';
import L from 'leaflet';
import { SensorDto } from '../../../../model/sensorDto';
import { UserService } from '../../../../service/user.service';
import { SensorDataService } from '../../../../service/sensorData.service';
import { SensorDataDto } from '../../../../model/sensorDataDto';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { InterestArea } from '../../../../model/interestArea';
import { InterestAreaService } from '../../../../service/interestArea.service';

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
 

  constructor(private http: HttpClient,private route: ActivatedRoute, private sensorDataService: SensorDataService, private interestAreaService : InterestAreaService) {}

 
  ngOnInit(): void {
    this.route.paramMap.subscribe((params: ParamMap) => {
      this.id = params.get('id');
      if (this.id) {
        this.loadData(this.id);
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

  private loadData(id: string | undefined): void {
    if (!id) {
      console.error('ID is undefined or null.');
      return;
    }

    this.sensorDataService.getSensorDataById(id).subscribe(
      (data: SensorDataDto) => {
        console.log('Sensor data loaded:', data);
      },
      (error: any) => {
        console.error('Error loading sensor data:', error);
      }
    );
  }

}