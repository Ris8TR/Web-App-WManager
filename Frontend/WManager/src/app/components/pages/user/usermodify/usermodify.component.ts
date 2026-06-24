import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {UserComponent} from "../userMenu/user.component";
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";
import {UserDto} from "../../../../model/userDto";
import {UserService} from "../../../../service/user.service";

@Component({
  selector: 'app-usermodify',
  standalone: true,
  imports: [
    CommonModule,
    UserComponent,
    FormsModule
  ],
  templateUrl: './usermodify.component.html',
  styleUrl: './usermodify.component.css'
})
export class UsermodifyComponent implements OnInit {

  protected userDto: any = {
    firstName: '',
    lastName: '',
    password: ''
  };

  isSaved: boolean = false;

  constructor(
    private toolbar: ToolbarComponent,
    private router: Router,
    private userService: UserService,
  ) { }

  ngOnInit(): void {
    this.toolbar.refreshToken();
    this.loadUserData();
  }

  private loadUserData() {
    const savedUser = JSON.parse(localStorage.getItem('user') || '{}');
    this.userDto = { ...savedUser, password: '' };
  }

  protected redirectToUserData() {
    // Torna alla visualizzazione profilo
    this.router.navigate(['/profile']);
  }


  protected onUpdateData() {
    if (this.isSaved) return;
    if (this.userDto.firstName && this.userDto.lastName) {
      this.userService.updatePrivateUser(this.userDto).subscribe({
        next: (updatedUser: UserDto) => {
          this.isSaved = true;
          console.log('Utente aggiornato con successo:', updatedUser);

          setTimeout(() => {
            this.router.navigate(['/userData']);
          }, 3000);
        },
        error: (err) => {
          console.error('Errore durante l\'aggiornamento dell\'utente:', err);
        },
      });
    }
  }
}
