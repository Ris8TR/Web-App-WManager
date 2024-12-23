export * from './auth.service';
import { AuthService } from './auth.service';
export * from './interestArea.service';
import { InterestAreaService } from './interestArea.service';
export * from './sensorData.service';
import { SensorDataService } from './sensorData.service';
export * from './user.service';
import { UserService } from './user.service';
export * from './userPreference.service';
import { UserPreferenceService } from './userPreference.service';
export const APIS = [AuthService, InterestAreaService, SensorDataService, UserService, UserPreferenceService];
