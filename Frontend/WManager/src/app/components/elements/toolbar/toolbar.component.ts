import { Component } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatIconModule, MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';
import { CommonModule } from '@angular/common';
import { MatButton } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import {MatButtonModule} from '@angular/material/button';
import { InterestAreaService } from '../../../service/interestArea.service';
import { InterestAreaDto } from '../../../model/interestAreaDto';


@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [MatToolbarModule, RouterOutlet, RouterLink, MatIconModule, HttpClientModule, CommonModule, MatButton, MatMenuModule, MatButtonModule],
  templateUrl: './toolbar.component.html',
  styleUrl: './toolbar.component.css'
})
export class ToolbarComponent {

  pageNumber: number = 0;
  logStringResult: string = "Login";
  productDetails = [[] as any];
  showLoadButton = false;
  interestAreaNames: InterestAreaDto[] = [ ];

  userId=""
  role: string = this.cookieService.get("role");

  constructor(private cookieService: CookieService,
    private router: Router,
    private interestAreaService: InterestAreaService,
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


  loadInterestAreas(): void {
    this.userId=this.cookieService.get("Token")
    this.interestAreaService.getInterestAreasByUser(this.userId).subscribe(
      (interestAreas: InterestAreaDto[]) => {
        this.interestAreaNames = [];
        interestAreas.forEach((area: InterestAreaDto) => {
          if (area.id && area.name) {
            this.interestAreaNames.push({ id: area.id, name: area.name });
          }
        });
      },
      (error: any) => {
        console.error('Error loading interest area data:', error);
      }
    );
  }

  
redirectToInterestArea(id: string) {
  this.router.navigate(['/detail', id]);
}


  redirectToProfile() {
    const userCookie = this.cookieService.get('Token');
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

  logOut() {
    this.cookieService.delete('user');
    this.cookieService.delete('role');
    this.cookieService.delete('Token');
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
}
