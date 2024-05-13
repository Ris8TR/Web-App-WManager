import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { SigninComponent } from './components/signin/signin.component';
import { MapComponent } from './components/map/observations/map.component';
import { ForecastComponent } from './components/map/forecast/forecast.component';


export const routes: Routes = [
    {path: '', redirectTo: "/home", pathMatch : "full"},
    {path: 'home',title:"Home", component: ForecastComponent},
    {path: 'ground-stations',title:"Graunded Stations", component: MapComponent},
    {path: 'login',title:"login", component: LoginComponent},
    {path: 'signin',title:"signin", component: SigninComponent},
    { path: '**', component: MapComponent }
  ];

