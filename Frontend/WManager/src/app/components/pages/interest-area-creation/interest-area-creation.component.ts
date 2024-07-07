import { Component } from '@angular/core';

import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NewSensorDataDto} from "../../../model/newSensorDataDto";
import {SensorDataService} from "../../../service/sensorData.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {CookieService} from "ngx-cookie-service";
import {Router} from "@angular/router";
import {NewInterestAreaDto} from "../../../model/newInterestAreaDto";
import {F} from "@angular/cdk/keycodes";
import {InterestAreaService} from "../../../service/interestArea.service";
import {InterestArea} from "../../../model/interestArea";
import {ToolbarComponent} from "../../elements/toolbar/toolbar.component";

@Component({
  selector: 'app-interest-area-creation',
  standalone: true,
    imports: [
        FormsModule,
        ReactiveFormsModule
    ],
  templateUrl: './interest-area-creation.component.html',
  styleUrl: './interest-area-creation.component.css'
})
export class InterestAreaCreationComponent {

  file!: File;
  data: NewInterestAreaDto = {name: "",     userId: this.cookieService.get("Token")};

  constructor(
    private interestAreaService: InterestAreaService,
    private snackBar: MatSnackBar,
    private cookieService: CookieService,
    private router: Router,
    private toolbar: ToolbarComponent
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
      this.interestAreaService.createInterestAreaForm(this.data, this.file).subscribe(
        response => {
          this.snackBar.open("Area caricata e accettata!", 'OK');
          console.log(response);
          this.toolbar.loadInterestAreas()
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

