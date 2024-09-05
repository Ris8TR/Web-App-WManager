import {Component, OnInit} from '@angular/core';
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { MatSnackBar } from "@angular/material/snack-bar";
import { CookieService } from "ngx-cookie-service";
import { Router } from "@angular/router";

import { MatListModule } from "@angular/material/list";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from "@angular/common";

import {error} from "@angular/compiler-cli/src/transformers/util";
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";
import {style} from "@angular/animations";
import {UserComponent} from "../userMenu/user.component";
import {FileType} from "../../../../model/enum/FileType";
import {NewSensorDto} from "../../../../model/newSensorDto";
import {SensorService} from "../../../../service/sensor.service";
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";
import {InterestAreaService} from "../../../../service/interestArea.service";
import {InterestAreaDto} from "../../../../model/interestAreaDto";


@Component({
  selector: 'app-sensor-creation',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatListModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    CommonModule,
    MatRadioGroup,
    MatRadioButton,
    UserComponent
  ],
  templateUrl: './sensor-creation.component.html',
  styleUrl: './sensor-creation.component.css'
})
export class SensorCreationComponent implements  OnInit{

  fileTypes = Object.values(FileType);
  data: NewSensorDto = {
    companyName: "",
    password: "",
    description: "",
    interestAreaId: "",
  };

  interestAreaNames: { name: string | undefined; id: string | undefined }[] = [];

  constructor(
    private sensorService: SensorService,
    private snackBar: MatSnackBar,
    private cookieService: CookieService,
    private router: Router,
    private toolbar: ToolbarComponent,
    private interestAreaService: InterestAreaService
  ) { }

  loadData() {
    console.log("Selected Interest Area ID:", this.data.interestAreaId); // Add this log for debugging
    this.toolbar.refreshToken().then(r =>
      this.data.token = this.cookieService.get("token"))
    this.sensorService.addSensor(this.data).subscribe(
      response => {
        this.snackBar.open("Sensore caricato e accettato!", 'OK');
        this.router.navigate(['/']);
      },
      error => {
        this.snackBar.open("Errore nel caricamento del sensore. Riprovare.", 'OK');
        console.error(error);
      }
    );
  }

  loadInterestAreas(): void {
    const userId = this.cookieService.get("token");
    this.interestAreaService.getInterestAreasByUser(userId).subscribe(
      (interestAreas: InterestAreaDto[]) => {
        this.interestAreaNames = interestAreas.map(area => ({ id: area.id, name: area.name }));
        console.log(interestAreas)
        },
      error => {
        console.error('Errore nel caricamento delle aree di interesse:', error);
      }
    );
  }

  ngOnInit(): void {
    this.loadInterestAreas();
  }

  protected readonly style = style;

  controllo() {
    if (!this.data) {
      this.snackBar.open("Devi compilare tutti i campi!", 'OK');
      return false;
    }
    return true;
  }
}
