import { Component } from '@angular/core';
import {UserComponent} from "../userMenu/user.component";
import {FormsModule} from "@angular/forms";
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";
import {NgForOf} from "@angular/common";

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
export class UsermodifyComponent {

}
