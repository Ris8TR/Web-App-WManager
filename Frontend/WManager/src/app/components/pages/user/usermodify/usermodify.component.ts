import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {UserComponent} from "../userMenu/user.component";
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";

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

  // Inizializziamo l'oggetto per evitare errori "undefined" nel template
  protected userDto: any = {
    firstName: '',
    lastName: '',
    password: ''
  };

  constructor(
    private toolbar: ToolbarComponent,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.toolbar.refreshToken();
    this.loadUserData();
  }

  private loadUserData() {
    // Qui dovresti caricare i dati dell'utente dal tuo Service
    // Esempio placeholder:
    const savedUser = JSON.parse(localStorage.getItem('user') || '{}');
    this.userDto = { ...savedUser, password: '' };
  }

  protected redirectToUserData() {
    // Torna alla visualizzazione profilo
    this.router.navigate(['/profile']);
  }

  protected onUpdateData() {
    if (this.userDto.firstName && this.userDto.lastName) {
      console.log('Aggiornamento dati in corso...', this.userDto);

      // Qui chiamerai il tuo UserService.update(this.userDto)...
      // Esempio di successo:
      // alert('Profilo aggiornato con successo!');
      // this.redirectToUserData();
    }
  }
}
