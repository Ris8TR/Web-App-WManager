import { Component, Inject } from '@angular/core';

import { MatSnackBar } from '@angular/material/snack-bar';
import { CookieService } from 'ngx-cookie-service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { NewUserDto } from '../../../../model/newUserDto';
import { UserService } from '../../../../service/user.service';


@Component({
  selector: 'app-signin',
  standalone: true,
  imports: [FormsModule,CommonModule,MatInputModule,MatButtonModule,MatFormFieldModule],
  templateUrl: './signin.component.html',
  styleUrl: './signin.component.css'
})
export class SigninComponent {

  constructor(
    private userService: UserService,
    private snackBar: MatSnackBar,
    private cookieSevices: CookieService,
    private router: Router,
  ) {}


  passwordCheckUser? : '';
  passwordCheckManu? : '';

  userIsPresent: Boolean =false
  manuIsPresent: Boolean =false


  newUser: NewUserDto = {
    sensorPassword: "",
    email: '',
    firstName: '',
    lastName: '',
    password:''
  };
  validateUser(): boolean {
    if (
      this.newUser.email &&
      this.newUser.firstName &&
      this.newUser.lastName &&
      this.newUser.password &&
      this.passwordCheckUser &&
      this.newUser.password === this.passwordCheckUser &&
      this.newUser.sensorPassword
    ) {
      return true;
    } else {
      this.snackBar.open("Fill in all the fields and make sure the passwords match", 'OK', { duration: 3000 });
      return false;
    }
  }


  userSignIn() {
    if (this.validateUser()) {
      this.userService.addUser(this.newUser, 'body').subscribe(
        (data) => {
          this.snackBar.open('User account successfully created', 'OK');
          this.router.navigate(['/login']);
        },
        (error) => {
          if (error.status === 409) { // Verifica il nuovo codice di stato 409
            this.userIsPresent = true;
            this.snackBar.open(
              'The email address has already been used. It is recommended to Log In',
              'OK',
              { duration: 3000 }
            );
          }
        }
      );
    }
  }

}
