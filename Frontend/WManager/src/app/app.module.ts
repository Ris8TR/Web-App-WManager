import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms'; // Import here

import { AppComponent } from './app.component';
import { AuthService } from './service/auth.service';
import { UserService } from './service/api';
import { ToolbarComponent } from './components/elements/toolbar/toolbar.component';
import { LoginComponent } from './components/pages/auth/login/login.component';
import { SigninComponent } from './components/pages/auth/signin/signin.component';
import {ResetComponent} from "./components/pages/auth/reset/reset.component";
import {SensorService} from "./service/sensor.service";
import {routes} from "./app.routes";
import {RouterModule} from "@angular/router";


@NgModule({
  declarations: [],
  imports: [BrowserModule, FormsModule,AppComponent,ToolbarComponent,LoginComponent,SigninComponent, ResetComponent,ToolbarComponent, RouterModule.forRoot(routes, { useHash: false }) ],
  providers: [AuthService,UserService,SensorService, ToolbarComponent],
  bootstrap: []
})
export class AppModule { }
