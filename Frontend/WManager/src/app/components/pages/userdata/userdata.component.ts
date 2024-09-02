import {Component, OnInit} from '@angular/core';
import {UserComponent} from "../userMenu/user.component";
import {Router, RouterOutlet} from "@angular/router";
import {FormsModule} from "@angular/forms";
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";
import {NgForOf} from "@angular/common";
import {ToolbarComponent} from "../../elements/toolbar/toolbar.component";

@Component({
  selector: 'app-userdata',
  standalone: true,
  imports: [
    UserComponent,
    RouterOutlet,
    FormsModule,
    MatRadioButton,
    MatRadioGroup,
    NgForOf
  ],
  templateUrl: './userdata.component.html',
  styleUrl: './userdata.component.css'
})
export class UserdataComponent implements OnInit{

  constructor(    private toolbar: ToolbarComponent) {

  }


  ngOnInit(): void {
    this.toolbar.refreshToken()
  }


}
