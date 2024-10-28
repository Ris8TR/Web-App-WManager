import {Component, OnInit} from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatIconModule, MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';
import { CommonModule } from '@angular/common';
import { MatButton } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule} from '@angular/material/button';
import { InterestAreaService } from '../../../service/interestArea.service';
import { InterestAreaDto } from '../../../model/interestAreaDto';
import {AuthService} from "../../../service/auth.service";
import * as jwt_decode from 'jwt-decode';
import {jwtDecode} from "jwt-decode";



@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [MatToolbarModule, RouterOutlet, RouterLink, MatIconModule, HttpClientModule, CommonModule, MatButton, MatMenuModule, MatButtonModule],
  templateUrl: './toolbar.component.html',
  styleUrl: './toolbar.component.css'
})
export class ToolbarComponent implements  OnInit{


  id: string = "";
  logStringResult: string = "Login";
  productDetails = [[] as any];
  showLoadButton = false;
  interestAreaNames: InterestAreaDto[] = [ ];

  userId=""
  role: string = this.cookieService.get("role");

  constructor(private cookieService: CookieService,
    private router: Router,
    private interestAreaService: InterestAreaService,
              private authService: AuthService,

              iconRegistry: MatIconRegistry, domSanitizer: DomSanitizer) {
    iconRegistry.addSvgIconSet(
      domSanitizer.bypassSecurityTrustResourceUrl('./assets/mdi.svg')
    );
  }

  ngOnInit(): void {
    this.checkUserCookie();
    if (this.logStringResult != "Login")
      this.loadInterestAreas();
  }

  async refreshToken(): Promise<void> {
    const refreshToken = this.cookieService.get("refreshToken");
    const token = this.cookieService.get("token");

    if (token && !this.isTokenExpired(token)) {
      // Il token non è scaduto, non è necessario rinfrescarlo
      return;
    }

    try {
      const tokenDto = await this.authService.refreshToken(refreshToken).toPromise();
      this.cookieService.set("token", tokenDto?.token!);
      this.cookieService.set("refreshToken", tokenDto?.refreshToken!);
    } catch (error) {
      throw new Error("Error refreshing token");
    }
  }

  private isTokenExpired(token: string): boolean {
    try {
      const decodedToken: any = jwtDecode(token);
      const currentTime = Math.floor(Date.now() / 1000); // Ottieni il tempo attuale in secondi

      return decodedToken.exp < currentTime;
    } catch (error) {
      // Se c'è un errore nel decodificare il token, presupponiamo che sia scaduto
      return true;
    }
  }


  loadInterestAreas(): void {
    this.userId=this.cookieService.get("token")
    this.interestAreaService.getInterestAreasByUser(this.userId).subscribe(
      (interestAreas: InterestAreaDto[]) => {
        this.interestAreaNames = [];
        interestAreas.forEach((area: InterestAreaDto) => {
          if (area.id || area.name) {
            this.interestAreaNames.push({ id: area.id, name: area.name });
          }
          console.log(this.interestAreaNames)
        });
      },
      (error: any) => {
        console.error('Error loading interest area data:', error);
      }
    );
  }


redirectToInterestArea(id: string) {
    this.router.navigate(['/interestAreaViewer', id]);
}

  redirectToCreateArea() {
  this.router.navigate(['/create-area']);
}


  redirectToProfile() {
    const userCookie = this.cookieService.get('token');
    if (!userCookie) {
      this.router.navigate(['/login']);
    } else {
      this.router.navigate(['/test/sensor']);
    }
  }

  redirectToGroundStations() {
    this.router.navigate(['/ground-stations']);
  }

  redirectToWebcams() {
    this.router.navigate(['/webcam']);
  }

  redirectToUser() {
    this.router.navigate(['/user']);
  }

  logOut() {
    this.cookieService.delete('user');
    this.cookieService.delete('role');
    this.cookieService.delete('token');
    this.cookieService.delete('refreshToken');
    this.cookieService.delete('sessionId');
    this.checkUserCookie();
    this.router.navigate(['/home']);
  }

  checkUserCookie(): void {
    const userCookie = this.cookieService.get('user');
    this.role = this.cookieService.get('role');

    if (userCookie) {
      this.logStringResult = userCookie;
    } else {
      this.logStringResult = 'Login';

    }

  }

  redirectToInterestAreaViewer() {
    this.router.navigate(["/interestAreaViewer"]);
  }
}
