import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { SigninComponent } from './components/signin/signin.component';
import { MapComponent } from './components/map/observations/map.component';


export const routes: Routes = [
    {path: '', redirectTo: "/home", pathMatch : "full"},
    {path: 'home',title:"Home", component: MapComponent},
    {path: 'login',title:"login", component: LoginComponent},
    {path: 'signin',title:"signin", component: SigninComponent},
    { path: '**', component: MapComponent }
  ];

