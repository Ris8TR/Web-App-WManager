import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { LoginDto } from '../model/loginDto';
import { NewUserDto } from '../model/newUserDto';
import { AuthService } from '../service/auth.service';
import { CookieService } from "ngx-cookie-service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { MatDialog } from "@angular/material/dialog";
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule,CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  modalElement: any;

  constructor(
    private authService: AuthService,
    private cookieService: CookieService, 
    private router: Router, 
    private snackBar: MatSnackBar,
    private dialog: MatDialog ) {}

  
  showResetPasswordModal: boolean = false;
  loginUser: LoginDto = {
    email: '',
    password: ''
  };

  loginManu: LoginDto = {
    email: '',
    password: ''
  };


  newUser: NewUserDto = {
    email: '',
    firstName: '',
    lastName: '',
    password:''

  };


  handleClickOutside(event: Event) {
    const clickedInside = this.modalElement?.nativeElement.contains(event.target);
    if (!clickedInside) {
      this.showResetPasswordModal = false;
    }
  }

  

  logInUser() {
    this.authService.loginUser(this.loginUser.email!, this.loginUser.password!, 'body').subscribe(
      (data) => {
        const token = data.token;
        this.cookieService.set('Token', data.token)
        this.cookieService.set('user', this.loginUser.email!)
        this.cookieService.set('role', "USER")
        console.log("GG")
        this.snackBar.open("Autenticazione avvenuta con successo", 'OK',{ duration: 3000 });
        this.router.navigate(['/home']);
      },
      (error) => {
        this.snackBar.open("Email o Passworderrati", 'RIPROVARE', { duration: 3000 });
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

  
  resetPass(): void {
    this.authService.resetPass(this.resetPasswordEmail, 'body').subscribe(
      (data) => {
        console.log("GG")
        this.snackBar.open("Email inviata con successo", 'OK');
        this.showResetPasswordModal=false
        this.router.navigate(['/login']);
      },
      (error) => {
        // Handle login error
        console.error('Error logging in:', error);
      }
    );
  }

}
