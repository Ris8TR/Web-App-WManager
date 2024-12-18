import { Component, OnInit } from '@angular/core';
import { UserComponent } from "../userMenu/user.component";
import { RouterOutlet } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { MatRadioButton, MatRadioGroup } from "@angular/material/radio";
import { NgForOf } from "@angular/common";
import { ToolbarComponent } from "../../../elements/toolbar/toolbar.component";
import { UserService } from "../../../../service/user.service";
import { CookieService } from "ngx-cookie-service";
import { UserDto } from "../../../../model/userDto";

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
    private cookieService: CookieService
  ) {}

  ngOnInit(): void {
    this.toolbar.refreshToken();

    const email = this.cookieService.get("user");
    if (email) {
      this.userService.findByEmail(email).subscribe({
        next: (user: UserDto) => this.userDto = user,
        error: (err) => console.error("Error getting user:", err)
      });
    } else {
      console.warn("No cookie.");
    }
  }
}
