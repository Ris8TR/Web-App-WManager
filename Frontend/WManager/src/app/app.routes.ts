import { Routes } from '@angular/router';
import { MapComponent } from './components/map/map.component';
import { LoginComponent } from './login/login.component';

export const routes: Routes = [
    {path: '', redirectTo: "/home", pathMatch : "full"},
    {path: 'home',title:"Home", component: MapComponent},
    {path: 'login',title:"login", component: LoginComponent},
    { path: '**', component: MapComponent }
  ];

