import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms'; // Import here

import { AppComponent } from './app.component';
import { AuthService } from './service/auth.service';
import { UserService } from './service/api';
import { LoginComponent } from './components/login/login.component';
import { SigninComponent } from './components/signin/signin.component';
import { ToolbarComponent } from './components/toolbar/toolbar.component';

@NgModule({
  declarations: [],
  imports: [BrowserModule, FormsModule,AppComponent,ToolbarComponent,LoginComponent,SigninComponent ], 
  providers: [AuthService,UserService],
  bootstrap: []
})
export class AppModule { }