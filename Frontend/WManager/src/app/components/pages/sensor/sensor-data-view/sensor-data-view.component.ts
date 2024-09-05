import { Component } from '@angular/core';
import {SensorDataService} from "../../../../service/sensorData.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";
import {CookieService} from "ngx-cookie-service";
import {Router} from "@angular/router";
import {SensorService} from "../../../../service/sensor.service";

@Component({
  selector: 'app-sensor-data-view',
  standalone: true,
  imports: [],
  templateUrl: './sensor-data-view.component.html',
  styleUrl: './sensor-data-view.component.css'
})
export class SensorDataViewComponent {

  token= ""

  constructor(
    private sensorService: SensorService,
    private snackBar: MatSnackBar,
    private toolbar: ToolbarComponent,
    private cookieService: CookieService,
  ) { }


  loadData() {

    this.toolbar.refreshToken().then(r =>{ this.token  = this.cookieService.get("token")} )
    this.sensorService.findByUserId(this.token).subscribe(
      response => {
        this.snackBar.open("Pizza e fichi", 'OK');
        console.log(response);
      },
      error => {
        this.snackBar.open("Errore. Riprovare.", 'OK');
        console.log(error);
      }
    );
  }

}
