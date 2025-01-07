import {Component, OnInit} from '@angular/core';
import {InterestAreaDto} from "../../../../model/interestAreaDto";
import {FormsModule} from "@angular/forms";
import {InterestAreaService} from "../../../../service/interestArea.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {CookieService} from "ngx-cookie-service";
import {NgForOf, NgIf} from "@angular/common";
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";
import {UserComponent} from "../../user/userMenu/user.component";
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";
import {
  DeleteConfirmationDialogComponent
} from "../../../actions/delete-confirmation-dialog/delete-confirmation-dialog.component";
import {MatDialog, MatDialogModule} from "@angular/material/dialog";

@Component({
  selector: 'app-interest-area-data-view',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    NgForOf,
    UserComponent,
    MatRadioButton,
    MatDialogModule,
    DeleteConfirmationDialogComponent,
    MatRadioGroup
  ],
  templateUrl: './interest-area-data-view.component.html',
  styleUrl: './interest-area-data-view.component.css'
})
export class InterestAreaDataViewComponent implements  OnInit{
  token = "";
  interestAreaList: InterestAreaDto[] = [];
  private geometryFile: any;
  private previewFile: any;


  constructor(
    private interestAreaService: InterestAreaService,
    private snackBar: MatSnackBar,
    private toolbar: ToolbarComponent,
    private cookieService: CookieService,
    private dialog: MatDialog
  ) { }

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.toolbar.refreshToken().then(r => {
    this.interestAreaService.getInterestAreasByUser().subscribe(
      response => {
        this.interestAreaList = response.map((interestArea: InterestAreaDto) => ({ ...interestArea, isEditing: false }));
      },
      error => {
        this.snackBar.open("Errore durante il caricamento dei dati.", 'OK');
        console.log(error);
      }
    );
  })
  }

  startEdit(interestArea: InterestAreaDto) {
    interestArea.isEditing = true;
  }

  saveEdit(interestArea: InterestAreaDto) {
    this.toolbar.refreshToken().then(r => {
      interestArea.token = this.cookieService.get("token");
    this.interestAreaService.updateInterestArea(interestArea, this.geometryFile, this.previewFile).subscribe(
      () => {
        interestArea.isEditing = false;
        this.toolbar.loadInterestAreas()
        this.snackBar.open("Dati aggiornati con successo", 'OK');
      },
      error => {
        this.snackBar.open("Errore durante il salvataggio dei dati.", 'OK');
        console.log(error);
      }
    );
  })}

  cancelEdit(interestArea: InterestAreaDto) {
    interestArea.isEditing = false;
    this.loadData();
  }

  loadGeometry(event: any) {
    const fileInput = event.target;
    const files = fileInput.files;

    if (files && files.length > 0) {
      const file = files[0];
      const allowedTypes = ['image/jpeg', 'image/png'];

      if (!allowedTypes.includes(file.type)) {
        alert('Invalid file type. Please select a JPG or PNG file.');
        fileInput.value = '';
        return;
      }

      this.geometryFile = file;
      console.log('Geometry file loaded:', file.name);
    } else {
      alert('No file selected. Please select a valid JPG or PNG file.');
    }
  }

  loadPreview(event: any) {
    const fileInput = event.target;
    const files = fileInput.files;

    if (files && files.length > 0) {
      const file = files[0];
      const allowedTypes = ['image/jpeg', 'image/png'];

      if (!allowedTypes.includes(file.type)) {
        alert('Invalid file type. Please select a JPG or PNG file.');
        fileInput.value = '';
        return;
      }

      this.previewFile = file;
      console.log('Preview file loaded:', file.name);
    } else {
      alert('No file selected. Please select a valid JPG or PNG file.');
    }
  }


  delete(interestArea: InterestAreaDto) {
    const dialogRef = this.dialog.open(DeleteConfirmationDialogComponent);
    dialogRef.afterClosed().subscribe((result: boolean) => {
      if (result) {
        // @ts-ignore
        this.interestAreaService.deleteInterestArea(interestArea.id).subscribe(() => {
            interestArea.isEditing = false;
            this.loadData();
            this.toolbar.loadInterestAreas();
            this.snackBar.open("Area eliminata", 'OK');
          },
          error => {
            this.snackBar.open("Errore durante l'eliminazione.", 'OK');
            console.log(error);
          });
      } else {
        this.snackBar.open("Eliminazione annullata", 'OK');
      }
    });
  }
}
