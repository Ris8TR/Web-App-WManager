/**
 * OpenAPi WManager
 * OpenApi documentation for Spring Security
 *
 * OpenAPI spec version: 1.0
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

export interface NewSensorDataDto {
  sensorId?: string;
  Id?: string;
  timestamp?: Date;
  payloadType?: string;
  latitude?: number;
  longitude?: number;
  payload?: any;
  userId?: string;
  token?: string;
  interestAreaId?: string;
  sensorPassword: string;
}
