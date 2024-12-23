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
import { ObjectId } from './objectId';

export interface SensorData { 
    id?: ObjectId;
    userId: string;
    dataType: string;
    date?: Date;
    timestamp?: Date;
    type?: string;
    latitude: number;
    longitude: number;
}