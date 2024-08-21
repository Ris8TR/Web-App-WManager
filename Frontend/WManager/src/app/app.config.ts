import { ApplicationConfig } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { FormsModule } from '@angular/forms';
import { AuthService } from './service/auth.service';
import { provideHttpClient } from '@angular/common/http';
import { UserService } from './service/user.service';
import { InterestAreaService } from './service/interestArea.service';
import { SensorDataService } from './service/sensorData.service';
import {SensorService} from "./service/sensor.service";

//TODO QUI VANNO I SERVICES ORA
export const appConfig: ApplicationConfig = {
  providers: [provideAnimationsAsync(),provideRouter(routes),FormsModule,AuthService,UserService, InterestAreaService,SensorDataService, SensorService,provideHttpClient()]
};
