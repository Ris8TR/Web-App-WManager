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
import {InterestAreaDataService} from "./service/InterestAreaDataService";
import {
  DeleteConfirmationDialogComponent
} from "./components/actions/delete-confirmation-dialog/delete-confirmation-dialog.component";
import {MatDialogModule} from "@angular/material/dialog";
import {ImageService} from "./service/image.service";


@NgModule({
  declarations: [],
  imports: [BrowserModule, FormsModule, AppComponent, ToolbarComponent, LoginComponent, SigninComponent, ResetComponent, ToolbarComponent, MatDialogModule, RouterModule.forRoot(routes, {useHash: false}), DeleteConfirmationDialogComponent],
  providers: [AuthService,InterestAreaDataService,UserService,SensorService, ImageService, ToolbarComponent],
  bootstrap: [],
})
export class AppModule { }
