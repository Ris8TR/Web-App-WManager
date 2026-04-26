import { Component, OnInit } from '@angular/core';
import {SensorService} from "../../../../service/sensor.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";
import {CookieService} from "ngx-cookie-service";
import {SensorDto} from "../../../../model/sensorDto";
import {FormsModule} from "@angular/forms";
import {CommonModule} from "@angular/common";
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";
import {Router, RouterOutlet} from "@angular/router";
import {UserComponent} from "../../user/userMenu/user.component";
import {InterestAreaDto} from "../../../../model/interestAreaDto";
import {
  DeleteConfirmationDialogComponent
} from "../../../actions/delete-confirmation-dialog/delete-confirmation-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {MatTooltip} from "@angular/material/tooltip";

@Component({
  selector: 'app-sensor-data-view',
  standalone: true,
  templateUrl: './sensor-data-view.component.html',
  imports: [
    FormsModule,
    CommonModule,
    MatRadioButton,
    MatRadioGroup,
    UserComponent,
    MatTooltip
  ],
  styleUrls: ['./sensor-data-view.component.css']
})
export class SensorDataViewComponent implements OnInit {

  token = "";
  sensorList: SensorDto[] = [];
  tempPassword: any;

  constructor(
    private sensorService: SensorService,
    private snackBar: MatSnackBar,
    private toolbar: ToolbarComponent,
    private cookieService: CookieService,
    private router: Router,
    private dialog: MatDialog
  ) {
  }

  ngOnInit() {
    this.loadData();
    this.toolbar.refreshToken()
  }

  loadData() {
    this.toolbar.refreshToken().then(r => {
      this.sensorService.findByUserId().subscribe(
        response => {
          this.sensorList = response.map((sensor: SensorDto) => ({...sensor, isEditing: false}));
          console.log(response)
        },
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
    });
  }

  cancelEdit(sensor: SensorDto) {
    sensor.isEditing = false;
    this.loadData(); // Ricarica i dati originali
  }


  toggleSidebar() {
    document.getElementById('wrapper')?.classList.toggle('toggled');
  }

  redirectToUserData() {
    this.router.navigate(['/userData']);
  }

  redirectToUserModify() {
    this.router.navigate(['/userModify']);
  }

  redirectToUserSendData() {
    this.router.navigate(['/userSendData']);

  }

  redirectToUserCreateSensor() {
    this.router.navigate(['/userCreateSensor']);

  }

  redirectToSensorDataView() {
    this.router.navigate(['/Show-Sensor']);

  }

  redirectToInterestAreaDataView() {
    this.router.navigate(['/Show-Areas']);

  }

  delete(sensorDto: SensorDto) {
    const dialogRef = this.dialog.open(DeleteConfirmationDialogComponent);

    dialogRef.afterClosed().subscribe((result: boolean) => {
      if (result) {
        // Verifichiamo che l'ID esista prima di chiamare il servizio
        if (!sensorDto.id) {
          this.snackBar.open("Impossibile eliminare: ID sensore mancante.", 'OK');
          return;
        }

        this.sensorService.deleteSensor(sensorDto.id).subscribe({
          next: () => {
            // Aggiornamento ottimistico dell'interfaccia:
            // Filtriamo la lista locale così l'utente vede sparire il sensore immediatamente
            this.sensorList = this.sensorList.filter(s => s.id !== sensorDto.id);
            this.snackBar.open("Sensore eliminato con successo", 'OK');
          },
          error: (error: any) => {
            console.error("Errore durante l'eliminazione:", error);
            this.snackBar.open("Errore durante l'eliminazione. Riprovare.", 'OK');
          }
        });
      } else {
        // Caso in cui l'utente preme "Annulla" nel dialog
        this.snackBar.open("Eliminazione annullata", 'OK');
      }
    });
  }
}

