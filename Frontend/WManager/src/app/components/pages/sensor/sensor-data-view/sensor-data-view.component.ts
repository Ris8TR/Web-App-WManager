import { Component, OnInit } from '@angular/core';
import {SensorService} from "../../../../service/sensor.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";
import {CookieService} from "ngx-cookie-service";
import {SensorDto} from "../../../../model/sensorDto";
import {FormsModule} from "@angular/forms";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-sensor-data-view',
  standalone: true,
  templateUrl: './sensor-data-view.component.html',
  imports: [
    FormsModule,
    CommonModule
  ],
  styleUrls: ['./sensor-data-view.component.css']
})
export class SensorDataViewComponent implements OnInit {

  token = "";
  sensorList: SensorDto[] = [];

  constructor(
    private sensorService: SensorService,
    private snackBar: MatSnackBar,
    private toolbar: ToolbarComponent,
    private cookieService: CookieService,
  ) { }

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.toolbar.refreshToken().then(r => {
      this.token = this.cookieService.get("token");
      this.sensorService.findByUserId(this.token).subscribe(
        response => {
          this.sensorList = response.map((sensor: SensorDto) => ({ ...sensor, isEditing: false }));
console.log(response)        },
        error => {
          this.snackBar.open("Errore durante il caricamento dei dati.", 'OK');
          console.log(error);
        }
      );
    });
  }

  startEdit(sensor: SensorDto) {
    sensor.isEditing = true;
  }

  saveEdit(sensor: SensorDto) {
    this.toolbar.refreshToken().then(r => {
      sensor.token = this.cookieService.get("token");
      this.sensorService.updateSensor(sensor).subscribe(
        () => {
          sensor.isEditing = false;
          this.snackBar.open("Dati aggiornati con successo", 'OK');
        },
        error => {
          this.snackBar.open("Errore durante il salvataggio dei dati.", 'OK');
          console.log(error);
        }
      );
    }
    )}

  cancelEdit(sensor: SensorDto) {
    sensor.isEditing = false;
    this.loadData(); // Ricarica i dati originali
  }
}

