import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { ToolbarComponent } from "../../../elements/toolbar/toolbar.component";
import { CookieService } from "ngx-cookie-service";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CommonModule],
  templateUrl: './user.component.html',
  styleUrl: './user.component.css'
})
export class UserComponent implements OnInit {

  role: string = '';

  constructor(
    private router: Router,
    private toolbar: ToolbarComponent,
    private cookieService: CookieService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.toolbar.refreshToken();
    const rawRole = this.cookieService.get('role');
    if (rawRole) {
      this.role = rawRole.trim().toUpperCase();
    } else {
      this.role = '';
    }
    this.cdr.detectChanges();
  }


  /**
   * Navigazione - Sezione Profilo
   */
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

  /**
   * Navigazione - Sezione Monitoring
   */
  redirectToSensorDataView() {
    this.router.navigate(['/Show-Sensor']);
  }

  redirectToInterestAreaDataView() {
    this.router.navigate(['/Show-Areas']);
  }

  /**
   * Navigazione - Sezione Admin
   */
  redirectToAdminDashboard(): void {
    this.router.navigate(['/adminDashboard']);
  }

  /**
   * Logout
   */
  protected logout() {
    this.toolbar.logOut();
    this.toolbar.checkUserCookie();
  }
}
