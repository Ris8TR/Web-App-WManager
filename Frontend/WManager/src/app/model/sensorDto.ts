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
import {payloadType} from "./newSensorDto";

export interface SensorDto {
    companyName?: string;
    userId?: string;
    id?: string;
    password?: string;
    description?: string;
    payloadType?: string;
    type?: string;
    visibility?: boolean;
    interestAreaID?: string;
    longitude?: Array<number>;
    latitude?: Array<number>;
    token?: string;
  isEditing?: boolean;

  }
