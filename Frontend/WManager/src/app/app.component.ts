import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterOutlet } from '@angular/router';
import {} from '@angular/common/http';
import { MatButton } from '@angular/material/button';
import { ToolbarComponent } from './components/elements/toolbar/toolbar.component';
import { MapComponent } from './components/elements/map/observations/map.component';


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule,ToolbarComponent,
// TODO: `HttpClientModule` should not be imported into a component directly.
// Please refactor the code to add `provideHttpClient()` call to the provider list in the
// application bootstrap logic and remove the `HttpClientModule` import from this component.
MapComponent,RouterLink, RouterOutlet,],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'WManager';
}
