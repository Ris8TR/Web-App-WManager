import {Component, OnInit} from '@angular/core';
import {UserComponent} from "../userMenu/user.component";
import {FormsModule} from "@angular/forms";
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";
import {NgForOf} from "@angular/common";
import {MatSnackBar} from "@angular/material/snack-bar";
import {CookieService} from "ngx-cookie-service";
import {Router} from "@angular/router";
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";


@Component({
  selector: 'app-usermodify',
  standalone: true,
  imports: [
    UserComponent,
    FormsModule,
    MatRadioButton,
    MatRadioGroup,
    NgForOf
  ],
  templateUrl: './usermodify.component.html',
  styleUrl: './usermodify.component.css'
})
export class UsermodifyComponent implements  OnInit{

  constructor(
    private toolbar: ToolbarComponent,
  ) { }


  ngOnInit(): void {
    this.toolbar.refreshToken()
  }

}
