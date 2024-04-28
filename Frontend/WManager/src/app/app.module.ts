import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms'; // Import here

import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { AuthService } from './service/auth.service';

@NgModule({
  declarations: [AppComponent, LoginComponent,AuthService],
  imports: [BrowserModule, FormsModule], 
  providers: [AuthService],
  bootstrap: [AppComponent]
})
export class AppModule { }