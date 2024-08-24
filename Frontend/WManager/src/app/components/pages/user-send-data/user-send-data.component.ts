import { Component } from '@angular/core';
import {UserComponent} from "../userMenu/user.component";
import {FormsModule} from "@angular/forms";
import {NewSensorDataDto} from "../../../model/newSensorDataDto";
import {SensorDataService} from "../../../service/sensorData.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {CookieService} from "ngx-cookie-service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-user-send-data',
  standalone: true,
  imports: [
    UserComponent,
    FormsModule
  ],
  templateUrl: './user-send-data.component.html',
  styleUrl: './user-send-data.component.css'
})
export class UserSendDataComponent {
  file!: File;
  data: NewSensorDataDto = {
    payloadType: "",
    userId: this.cookieService.get("Token"),
    latitude: 0,
    longitude: 0,
    payload: {},
    sensorPassword: ""
  };

  interestAreas?: string[]


  constructor(
    private sensorDataService: SensorDataService,
    private snackBar: MatSnackBar,
    private cookieService: CookieService,
    private router: Router
  ) {
    if(!this.cookieService.get("token"))
    {
      //console.log("no log in ", this.cookieService.get("token"));
      this.router.navigate(['/']);
    }

  }

  caricaFile(event: any) {
    const fileInput = event.target;
    const files = fileInput.files;
    this.file = files[0];
  }

  controllo() {
    if (!this.data) {
      this.snackBar.open("Devi compilare tutti i campi!", 'OK');
      return false;
    }
    return true;
  }

  formValido() {
    return this.file != null;
  }

  loadData() {
    if (this.controllo()) {
      this.sensorDataService.saveSensorData(this.data, this.file).subscribe(
        response => {
          this.snackBar.open("Dato caricato e accettato!", 'OK');
          console.log(response);
          this.router.navigate(['/']);
        },
        error => {
          this.snackBar.open("Errore. Riprovare.", 'OK');
          console.log(error);
        }
      );
    }
  }
}
