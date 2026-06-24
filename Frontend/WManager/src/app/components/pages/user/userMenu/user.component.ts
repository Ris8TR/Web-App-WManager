import {Component, OnInit} from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";
import {CookieService} from "ngx-cookie-service";

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [RouterOutlet, RouterLink ],
  templateUrl: './user.component.html',
  styleUrl: './user.component.css'
})
export class UserComponent implements OnInit{
  constructor(private router: Router,     private toolbar: ToolbarComponent, private cookieService: CookieService) {
  }

  role: any = '';


  ngOnInit(): void {
    this.toolbar.refreshToken()
    this.role = this.cookieService.get('role') ;
    console.log(this.role);
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

  redirectToInterestAreaDataView() {
    this.router.navigate(['/Show-Areas']);

  }

  redirectToAdminDashboard(): void {
    this.router.navigate(['/admin/dashboard']);
  }

  protected logout() {
    this.toolbar.logOut()
    this.toolbar.checkUserCookie()

  }
}


