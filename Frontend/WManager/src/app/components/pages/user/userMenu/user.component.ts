import {Component, OnInit} from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [RouterOutlet, RouterLink ],
  templateUrl: './user.component.html',
  styleUrl: './user.component.css'
})
export class UserComponent implements OnInit{
  constructor(private router: Router,     private toolbar: ToolbarComponent) {

  }


  ngOnInit(): void {
    this.toolbar.refreshToken()
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

  redirectToSensorDataView() {
    this.router.navigate(['/Show-Sensor']);

  }
}


