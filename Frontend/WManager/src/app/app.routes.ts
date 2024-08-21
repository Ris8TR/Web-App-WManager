import { Routes } from '@angular/router';
import { ForecastComponent } from './components/elements/map/forecast/forecast.component';
import { MapComponent } from './components/elements/map/observations/map.component';
import { LoginComponent } from './components/pages/login/login.component';
import { SigninComponent } from './components/pages/signin/signin.component';
import { BadComponent } from './components/pages/Errors/badRequest/bad.component';
import { NotAuthorizedComponent } from './components/pages/Errors/not-authorized/not-authorized.component';
import { NotFoundComponent } from './components/pages/Errors/not-found/not-found.component';
import { RefusedComponent } from './components/pages/Errors/refused/refused.component';
import { DetailedAreaComponent } from './components/elements/map/detailed-area/detailed-area.component';
import { SensorDataUploadComponent } from './components/pages/sensor-data-upload/sensor-data-upload.component';
import { WebcamComponent } from './components/elements/map/webcam/webcam.component';
import {
  InterestAreaCreationComponent
} from "./components/pages/interest-area-creation/interest-area-creation.component";
import {ResetComponent} from "./components/pages/reset/reset.component";
import {SensorCreationComponent} from "./components/pages/sensor-creation/sensor-creation.component";



export const routes: Routes = [
    //Pages
    {path: '', redirectTo: "/home", pathMatch : "full"},
    {path: 'home',title:"Home", component: ForecastComponent},
    {path: 'ground-stations',title:"Graunded Stations", component: MapComponent},
    {path: 'login',title:"login", component: LoginComponent},
    {path: 'signin',title:"signin", component: SigninComponent},
    {path: 'detail/:id', title: "Area Details", component: DetailedAreaComponent },
    {path: 'test/sensor', title: "Test upload sensori", component: SensorDataUploadComponent },
    {path: 'webcam', title: "Webcam 'LIVE' ", component: WebcamComponent },
    {path: 'create-area', title: "Create new area' ", component: InterestAreaCreationComponent },
    {path: 'create-sensor', title: "Create new sensor' ", component: SensorCreationComponent },

  {path: 'reset/:token', title: "Password reset", component: ResetComponent },

    //Errors:
    {path: 'bad-request',title:"Bad Request", component: BadComponent},
    {path: 'not-authorized',title:"Not Authorized", component: NotAuthorizedComponent },
    {path: 'not-found',title:"Not Found", component: NotFoundComponent},
    {path: 'refused',title:"Refused", component: RefusedComponent},
    //Others:
    { path: '**', component: MapComponent }
  ];

