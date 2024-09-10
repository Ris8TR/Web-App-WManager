import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {UserComponent} from "../userMenu/user.component";
import {FormsModule} from "@angular/forms";

import {MatSnackBar} from "@angular/material/snack-bar";
import {CookieService} from "ngx-cookie-service";
import {Router} from "@angular/router";

import {MatRadioButton} from "@angular/material/radio";
import {NgForOf} from "@angular/common";
import {NewSensorDataDto} from "../../../../model/newSensorDataDto";
import {SensorDataService} from "../../../../service/sensorData.service";
import {SensorService} from "../../../../service/sensor.service";
import {InterestAreaService} from "../../../../service/interestArea.service";
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";
import {SensorAndAreas} from "../../../../model/sensorAndAreas";


@Component({
  selector: 'app-user-send-data',
  standalone: true,
  imports: [
    UserComponent,
    FormsModule,
    MatRadioButton,
    NgForOf
  ],
  templateUrl: './user-send-data.component.html',
  styleUrl: './user-send-data.component.css'
})
export class UserSendDataComponent implements  OnInit{
  @ViewChild('selectSensor') selectSensor!: ElementRef
  file!: File;
  data: NewSensorDataDto = {
    payloadType: "",
    token: this.cookieService.get("token"),
    latitude: 0,
    longitude: 0,
    payload: {},
    sensorPassword: "",
    sensorId: ""
  };

  selectedId!: string;

  interestAreas?: string[]
  endPointsInfo: {
    companyName: string | undefined;
    description: string | undefined;
    id: string | undefined;
    interestAreaID: string | undefined
  }[] | undefined = [];
  interestAreaInfo: { name: string | undefined;} = {name:""};


  constructor(
    private sensorDataService: SensorDataService,
    private sensorService: SensorService,
    private interestedAreaService: InterestAreaService,
    private snackBar: MatSnackBar,
    private cookieService: CookieService,
    private toolbar: ToolbarComponent,
    private router: Router
  ) {
    this.toolbar.refreshToken()
    if (!this.cookieService.get("token")) {
      //console.log("no log in ", this.cookieService.get("token"));
      this.router.navigate(['/']);
    }

  }

  selectedOption: any;

  ngOnInit(): void {
    this.loadEndPointsByUser();
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

  loadEndPointsByUser(): void {
    const userId = this.cookieService.get("token");
    this.sensorService.findAndAreaByUserId(userId).subscribe(
      (sensorAndAreas: SensorAndAreas) => {
        console.log(sensorAndAreas)
        // Mappiamo i dati dei sensori
        this.endPointsInfo = sensorAndAreas.sensorDtoList?.map(sensor => ({
          id: sensor.id,
          description: sensor.description,
          companyName: sensor.companyName,
          interestAreaID: sensor.interestAreaID
        }));

        const interestAreaInfoMap = new Map<string, string>();
        sensorAndAreas.areaDtoList?.forEach(area => {
          if (area.id != null && area.name != null ) {
              interestAreaInfoMap.set(area.id, area.name);

          }
        });

        // @ts-ignore
        const interestedAreaId = this.endPointsInfo[1].interestAreaID;
        if (interestedAreaId && interestAreaInfoMap.has(interestedAreaId)) {
          this.interestAreaInfo = { name: interestAreaInfoMap.get(interestedAreaId) };
        }
      },
      error => {
        console.error('Errore nel caricamento delle aree di interesse:', error);
      }
    );
  }




  loadData() {
    //if (this.controllo()) {
    this.toolbar.refreshToken().then(r => this.cookieService.get("token") )
      console.log(this.data.sensorId);
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

    onSelected(s :string): void{
      this.data.sensorId = s
      //console.log(s);
    }
  //}
}
