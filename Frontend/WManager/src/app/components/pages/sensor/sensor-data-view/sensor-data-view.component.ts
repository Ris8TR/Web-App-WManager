import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core'; // Aggiunto ChangeDetectorRef
import { SensorService } from "../../../../service/sensor.service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { ToolbarComponent } from "../../../elements/toolbar/toolbar.component";
import { CookieService } from "ngx-cookie-service";
import { SensorDto } from "../../../../model/sensorDto";
import { FormsModule } from "@angular/forms";
import { CommonModule } from "@angular/common";
import { MatRadioButton, MatRadioGroup } from "@angular/material/radio";
import { Router } from "@angular/router";
import { UserComponent } from "../../user/userMenu/user.component";
import { MatDialog } from "@angular/material/dialog";
import { MatTooltip } from "@angular/material/tooltip";
import { ScrollingModule } from "@angular/cdk/scrolling";
import { DeleteConfirmationDialogComponent } from "../../../actions/delete-confirmation-dialog/delete-confirmation-dialog.component";

@Component({
  selector: 'app-sensor-data-view',
  standalone: true,
  templateUrl: './sensor-data-view.component.html',
  styleUrls: ['./sensor-data-view.component.css'],
  imports: [
    FormsModule,
    CommonModule,
    MatRadioButton,
    MatRadioGroup,
    ScrollingModule,
    UserComponent,
    MatTooltip
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SensorDataViewComponent implements OnInit {

  sensorList: SensorDto[] = [];
  tempPassword: string = "";

  constructor(
    private sensorService: SensorService,
    private snackBar: MatSnackBar,
    private toolbar: ToolbarComponent,
    private cookieService: CookieService,
    private router: Router,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef // Iniettato per forzare l'aggiornamento della UI
  ) {}

  ngOnInit() {
    this.loadData();
    this.toolbar.refreshToken();
  }

  loadData() {
    this.toolbar.refreshToken().then(() => {
      this.sensorService.findByUserId().subscribe({
        next: (response) => {
          this.sensorList = response.map((sensor: SensorDto) => ({ ...sensor, isEditing: false }));
          this.cdr.markForCheck(); // <--- FONDAMENTALE: Notifica Angular che i dati sono pronti
        },
        error: (error) => {
          this.snackBar.open("Errore durante il caricamento dei dati.", 'OK');
          console.error(error);
        }
      });
    });
  }

  startEdit(sensor: SensorDto) {
    sensor.isEditing = true;
    this.cdr.markForCheck();
  }

  saveEdit(sensor: SensorDto) {
    this.toolbar.refreshToken().then(() => {
      sensor.token = this.cookieService.get("token");
      this.sensorService.updateSensor(sensor).subscribe({
        next: () => {
          sensor.isEditing = false;
          this.snackBar.open("Dati aggiornati con successo", 'OK');
          this.cdr.markForCheck();
        },
        error: (error) => {
          this.snackBar.open("Errore durante il salvataggio dei dati.", 'OK');
          console.error(error);
        }
      });
    });
  }

  cancelEdit(sensor: SensorDto) {
    sensor.isEditing = false;
    this.loadData();
  }

  delete(sensorDto: SensorDto) {
    const dialogRef = this.dialog.open(DeleteConfirmationDialogComponent);
    dialogRef.afterClosed().subscribe((result: boolean) => {
      if (result) {
        if (!sensorDto.id) {
          this.snackBar.open("Impossibile eliminare: ID sensore mancante.", 'OK');
          return;
        }
        this.sensorService.deleteSensor(sensorDto.id).subscribe({
          next: () => {
            this.sensorList = this.sensorList.filter(s => s.id !== sensorDto.id);
            this.snackBar.open("Sensore eliminato con successo", 'OK');
            this.cdr.markForCheck();
          },
          error: (error) => {
            this.snackBar.open("Errore durante l'eliminazione.", 'OK');
            console.error(error);
          }
        });
      }
    });
  }

  redirectToUserData() { this.router.navigate(['/userData']); }
  redirectToUserModify() { this.router.navigate(['/userModify']); }
  redirectToUserSendData() { this.router.navigate(['/userSendData']); }
  redirectToUserCreateSensor() { this.router.navigate(['/userCreateSensor']); }
  redirectToSensorDataView() { this.router.navigate(['/Show-Sensor']); }
  redirectToInterestAreaDataView() { this.router.navigate(['/Show-Areas']); }
}
