import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterOutlet } from '@angular/router';
import { ToolbarComponent } from './components/toolbar/toolbar.component';
import { HttpClientModule } from '@angular/common/http';
import { MapComponent } from './components/map/observations/map.component';


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule,ToolbarComponent,HttpClientModule,MapComponent,RouterLink, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'WManager';
}
