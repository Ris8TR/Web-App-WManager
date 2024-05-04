import { Routes } from '@angular/router';
import { MapComponent } from './components/map/map.component';
import { LoginComponent } from './components/login/login.component';
import { SigninComponent } from './components/signin/signin.component';


export const routes: Routes = [
    {path: '', redirectTo: "/home", pathMatch : "full"},
    {path: 'home',title:"Home", component: MapComponent},
    {path: 'login',title:"login", component: LoginComponent},
    {path: 'signin',title:"signin", component: SigninComponent},
    { path: '**', component: MapComponent }
  ];

