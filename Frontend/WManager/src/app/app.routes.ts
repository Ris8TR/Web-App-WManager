import { Routes } from '@angular/router';
import { ForecastComponent } from './components/elements/map/forecast/forecast.component';
import { MapComponent } from './components/elements/map/observations/map.component';
import { LoginComponent } from './components/pages/login/login.component';
import { SigninComponent } from './components/pages/signin/signin.component';
import { BadComponent } from './components/pages/Errors/badRequest/bad.component';
import { NotAuthorizedComponent } from './components/pages/Errors/not-authorized/not-authorized.component';
import { NotFoundComponent } from './components/pages/Errors/not-found/not-found.component';
import { RefusedComponent } from './components/pages/Errors/refused/refused.component';



export const routes: Routes = [
    {path: '', redirectTo: "/home", pathMatch : "full"},
    {path: 'home',title:"Home", component: ForecastComponent},
    {path: 'ground-stations',title:"Graunded Stations", component: MapComponent},
    {path: 'login',title:"login", component: LoginComponent},
    {path: 'signin',title:"signin", component: SigninComponent},
    //Errors:
    {path: 'bad-request',title:"Bad Request", component: BadComponent},
    {path: 'not-authorized',title:"Not Authorized", component: NotAuthorizedComponent },
    {path: 'not-found',title:"Not Found", component: NotFoundComponent},
    {path: 'refused',title:"Refused", component: RefusedComponent},
    //Others:
    { path: '**', component: MapComponent }
  ];

