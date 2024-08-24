import { Component } from '@angular/core';
import {UserComponent} from "../userMenu/user.component";
import {RouterOutlet} from "@angular/router";
import {FormsModule} from "@angular/forms";
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";
import {NgForOf} from "@angular/common";

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
export class UserdataComponent {

}
