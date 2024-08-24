import { Component } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [RouterOutlet, RouterLink ],
  templateUrl: './user.component.html',
  styleUrl: './user.component.css'
})
export class UserComponent {
  constructor(private router: Router) {

  }

  toggleSidebar() {
    document.getElementById('wrapper')?.classList.toggle('toggled');
  }

  redirectToUserData() {
    this.router.navigate(['/userData']);
  }

  redirectToUserModify() {
    this.router.navigate(['/userModify']);
  }

  redirectToUserSendData() {
    this.router.navigate(['/userSendData']);

  }

  redirectToUserCreateSensor() {
    this.router.navigate(['/userCreateSensor']);

  }
}


