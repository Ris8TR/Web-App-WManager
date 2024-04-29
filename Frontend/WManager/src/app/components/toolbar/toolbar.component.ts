import { Component } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import {MatIconModule, MatIconRegistry} from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';
import { CommonModule } from '@angular/common';



@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [MatToolbarModule,RouterOutlet, RouterLink,MatIconModule,HttpClientModule, CommonModule ],
  templateUrl: './toolbar.component.html',
  styleUrl: './toolbar.component.css'
})
export class ToolbarComponent {

  pageNumber: number = 0;
  logStringResult: string ="Login";
  productDetails = [[] as any];
  showLoadButton = false;
  role:string = this.cookieService.get("role")

  constructor(private cookieService: CookieService, 
    private router: Router,
    iconRegistry: MatIconRegistry, domSanitizer: DomSanitizer) {
    iconRegistry.addSvgIconSet(
      domSanitizer.bypassSecurityTrustResourceUrl('./assets/mdi.svg')
    );
  }

  ngOnInit(): void {
    this.checkUserCookie()
  }


  
  redirectToSearch(searchKeyword: string) {

    const formattedSearch = searchKeyword.toLowerCase();
    this.router.navigate(['/search', formattedSearch]);

  }

  redirectToProfile(){
    const userCookie = this.cookieService.get('user');

    if (!userCookie) {
      this.router.navigate(['/login']);
    } else {
      this.router.navigate(['/myprofile']);
    }
  }

  logOut(){
    this.cookieService.delete('user');
    this.cookieService.delete('role');
    this.cookieService.delete('Token');
    this.cookieService.delete('sessionId');
    this.checkUserCookie()
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
