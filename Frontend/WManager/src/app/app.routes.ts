import { Routes } from '@angular/router';
import { ForecastComponent } from './components/elements/map/forecast/forecast.component';
import { MapComponent } from './components/elements/map/observations/map.component';
import { LoginComponent } from './components/pages/auth/login/login.component';
import { SigninComponent } from './components/pages/auth/signin/signin.component';
import { BadComponent } from './components/pages/Errors/badRequest/bad.component';
import { NotAuthorizedComponent } from './components/pages/Errors/not-authorized/not-authorized.component';
import { NotFoundComponent } from './components/pages/Errors/not-found/not-found.component';
import { RefusedComponent } from './components/pages/Errors/refused/refused.component';
import { SensorDataUploadComponent } from './components/pages/sensor/sensor-data-upload/sensor-data-upload.component';
import { WebcamComponent } from './components/elements/map/webcam/webcam.component';
import {
  InterestAreaCreationComponent
} from "./components/pages/area/interest-area-creation/interest-area-creation.component";
import {ResetComponent} from "./components/pages/auth/reset/reset.component";

import {InterestAreaViewerComponent} from "./components/pages/area/interest-area-viewer/interest-area-viewer.component";
import {UserComponent} from "./components/pages/user/userMenu/user.component";
import {UserdataComponent} from "./components/pages/user/userdata/userdata.component";
import {UsermodifyComponent} from "./components/pages/user/usermodify/usermodify.component";
import {UserSendDataComponent} from "./components/pages/user/user-send-data/user-send-data.component";
import {SensorCreationComponent} from "./components/pages/user/user-sensor-creation/sensor-creation.component";
import {SensorDataViewComponent} from "./components/pages/sensor/sensor-data-view/sensor-data-view.component";
import {
  InterestAreaDataViewComponent
} from "./components/pages/area/interest-area-data-view/interest-area-data-view.component";
import {resolve} from "@angular/compiler-cli";



export const routes: Routes = [
    //Pages
    {path: '', redirectTo: "/forecast", pathMatch : "full"},
    {path: 'forecast',title:"Home", component: ForecastComponent},
    {path: 'ground-stations',title:"Graunded Stations", component: MapComponent},
    {path: 'login',title:"login", component: LoginComponent},
    {path: 'signing',title:"signing", component: SigninComponent},
    {path: 'user',title:"user", component: UserComponent},
    {path: 'userData',title:"user", component: UserdataComponent},
    {path: 'userModify',title:"user", component: UsermodifyComponent},
    {path: 'userSendData',title:"user", component: UserSendDataComponent},
    {path: 'sensorDataUpload', title: "Test upload sensori", component: SensorDataUploadComponent },
    {path: 'webcam', title: "Webcam 'LIVE' ", component: WebcamComponent },
    {path: 'create-area', title: "Create new area' ", component: InterestAreaCreationComponent },
    {path: 'userCreateSensor', title: "Create new sensor' ", component: SensorCreationComponent },
    {path: 'interestAreaViewer/:id', title: "", component: InterestAreaViewerComponent },
    {path: 'Show-Sensor', title: "", component: SensorDataViewComponent },
    {path: 'Show-Areas', title: "", component: InterestAreaDataViewComponent },

    {path: 'reset/:token', title: "Password reset", component: ResetComponent },

    //Errors:
    {path: 'bad-request',title:"Bad Request", component: BadComponent},
    {path: 'not-authorized',title:"Not Authorized", component: NotAuthorizedComponent },
    {path: 'not-found',title:"Not Found", component: NotFoundComponent},
    {path: 'refused',title:"Refused", component: RefusedComponent},
    //Others:
    { path: '**', component: NotFoundComponent },
    { path: '**/:**', component: NotFoundComponent, pathMatch: "full" }
  ];

