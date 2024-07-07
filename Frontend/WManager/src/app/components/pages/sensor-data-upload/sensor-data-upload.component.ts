import { Component } from '@angular/core';
import { NewInterestAreaDto } from '../../../model/newInterestAreaDto';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { UserService } from '../../../service/user.service';
import { Token } from '@angular/compiler';
import { NewSensorDataDto } from '../../../model/newSensorDataDto';
import { FormsModule } from '@angular/forms';
import { SensorDataService } from '../../../service/sensorData.service';

@Component({
  selector: 'app-sensor-data-upload',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './sensor-data-upload.component.html',
  styleUrl: './sensor-data-upload.component.css'
})
export class SensorDataUploadComponent {
  file!: File;
  data: NewSensorDataDto = {
    dataType: "",
    userId: this.cookieService.get("Token"),
    latitude: 0,
    longitude: 0,
    payload: {}
  };

  constructor(
    private sensorDataService: SensorDataService,
    private snackBar: MatSnackBar,
    private cookieService: CookieService,
    private router: Router
  ) { }

  caricaFile(event: any) {
    const fileInput = event.target;
    const files = fileInput.files;
    this.file = files[0];
  }

  controllo() {
    if (!this.data) {
      this.snackBar.open("Devi inserire un titolo", 'OK');
      return false;
    }
    return true;
  }

  formValido() {
    return this.file != null;
  }

  loadData() {
    if (this.controllo()) {
      this.sensorDataService.saveSensorData(this.data , this.file).subscribe(
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
