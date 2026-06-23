import { Component, OnInit } from '@angular/core';
import { UserComponent } from "../userMenu/user.component";
import {Router, RouterOutlet} from "@angular/router";
import { FormsModule } from "@angular/forms";
import { MatRadioButton, MatRadioGroup } from "@angular/material/radio";
import { NgForOf } from "@angular/common";
import { ToolbarComponent } from "../../../elements/toolbar/toolbar.component";
import { UserService } from "../../../../service/user.service";
import { CookieService } from "ngx-cookie-service";
import { UserDto } from "../../../../model/userDto";
import {MatSnackBar} from "@angular/material/snack-bar";
import {timeout} from "rxjs";

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
  styleUrls: ['./userdata.component.css'] // Corretto il nome della proprietà
})
export class UserdataComponent implements OnInit {

  userDto: UserDto = { lastName: "", firstName: "", email: "" };

  constructor(
    private toolbar: ToolbarComponent,
    private userService: UserService,
    private matSnackBar: MatSnackBar,
    private router: Router,
  ) {}


    ngOnInit(): void {
      this.toolbar.refreshToken();

      this.userService.getUser().subscribe({
        next: (user: UserDto) => {
          this.userDto = user;
        },
        error: (err) => console.error("Error getting user:", err, 10)
      });
    }


  protected openResetPasswordModal() {

  }
}
