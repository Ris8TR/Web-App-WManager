import { Component, Inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { CookieService } from "ngx-cookie-service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { MatDialog } from "@angular/material/dialog";
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {ElementRef, ViewChild } from '@angular/core';
import { LoginDto } from '../../../../model/loginDto';
import { AuthService } from '../../../../service/auth.service';
import { ToolbarComponent } from '../../../elements/toolbar/toolbar.component';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule,CommonModule,MatInputModule,MatButtonModule,MatFormFieldModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})

export class LoginComponent {

  @ViewChild('resetPasswordInput') resetPasswordInput!: ElementRef;
  modalElement: any;

  constructor(
    private authService: AuthService,
    private cookieService: CookieService,
    private router: Router,
    private toolbar:ToolbarComponent,
    private snackBar: MatSnackBar,
    private dialog: MatDialog ) {}


  showResetPasswordModal: boolean = false;
  loginUser: LoginDto = {
    email: '',
    password: ''
  };



  handleClickOutside(event: Event) {
    const clickedInside = this.modalElement?.nativeElement.contains(event.target);
    if (!clickedInside) {
      this.showResetPasswordModal = false;
    }
  }



  logInUser() {
    this.authService.loginUser(this.loginUser).subscribe(
      (token  ) => {
        this.cookieService.set('token', token.token!)
        this.cookieService.set('refreshToken', token.refreshToken!)
        this.cookieService.set('user', this.loginUser.email!)
        this.cookieService.set('role', "USER")
        this.snackBar.open("User logged in", 'OK',{ duration: 3000 });
        this.toolbar.checkUserCookie()
        this.toolbar.loadInterestAreas()
        this.router.navigate(['/home']);
      },
      (error: any) => {
        this.snackBar.open("Incorrect Email or Password", 'Try Again', { duration: 3000 });
        console.error('Error logging in:', error);
      }
    );
  }

  resetPasswordEmail: string = '';

  openResetPasswordModal(): void {
    this.showResetPasswordModal = !this.showResetPasswordModal;
  }

  closeModal(event: any) {
    if (event.target.classList.contains('reset-password-modal')) {
      this.openResetPasswordModal()


    }
  }

  stopClickPropagation(event: MouseEvent) {
    event.stopPropagation();
  }


  resetPass(): void {
    this.authService.resetPass(this.resetPasswordEmail, 'body').subscribe(
      (data: any) => {
        console.log("GG")
        this.snackBar.open("Email sent successfully", 'OK');
        this.showResetPasswordModal=false
        this.router.navigate(['/login']);
      },
      (error: any) => {
        // Handle login error
        console.error('Error logging in:', error);
      }
    );
  }

}
