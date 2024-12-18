import {Component, OnInit} from '@angular/core';
import {InterestAreaDto} from "../../../../model/interestAreaDto";
import {FormsModule} from "@angular/forms";
import {InterestAreaService} from "../../../../service/interestArea.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {CookieService} from "ngx-cookie-service";
import {NgForOf, NgIf} from "@angular/common";
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";
import {UserComponent} from "../../user/userMenu/user.component";

@Component({
  selector: 'app-interest-area-data-view',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    NgForOf,
    UserComponent
  ],
  templateUrl: './interest-area-data-view.component.html',
  styleUrl: './interest-area-data-view.component.css'
})
export class InterestAreaDataViewComponent implements  OnInit{
  token = "";
  interestAreaList: InterestAreaDto[] = [];

  constructor(
    private interestAreaService: InterestAreaService,
    private snackBar: MatSnackBar,
    private toolbar: ToolbarComponent,
    private cookieService: CookieService
  ) { }

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.toolbar.refreshToken().then(r => {
     this.token = this.cookieService.get("token");
    this.interestAreaService.getInterestAreasByUser(this.token).subscribe(
      response => {
        this.interestAreaList = response.map((interestArea: InterestAreaDto) => ({ ...interestArea, isEditing: false }));
        this.snackBar.open("Dati caricati con successo", 'OK');
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
    this.interestAreaService.updateArea(interestArea).subscribe(
      () => {
        interestArea.isEditing = false;
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
}
