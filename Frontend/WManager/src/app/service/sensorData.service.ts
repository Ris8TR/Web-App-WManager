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
 *//* tslint:disable:no-unused-variable member-ordering */

import { Inject, Injectable, Optional }                      from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams,
         HttpResponse, HttpEvent }                           from '@angular/common/http';
import { CustomHttpUrlEncodingCodec }                        from '../encoder';

import { Observable }                                        from 'rxjs';

import { NewSensorDataDto } from '../model/newSensorDataDto';
import { SensorData } from '../model/sensorData';
import { SensorDataDto } from '../model/sensorDataDto';
import { ServiceError } from '../model/serviceError';

import { BASE_PATH, COLLECTION_FORMATS }                     from '../variables';
import { Configuration }                                     from '../configuration';
import { V1SensorDataBody } from '../model/v1SensorDataBody';


@Injectable()
export class SensorDataService {

  protected basePath = 'http://192.168.15.34:8010';
  public defaultHeaders = new HttpHeaders();
  public configuration = new Configuration();

  constructor(protected httpClient: HttpClient, @Optional() @Inject(BASE_PATH) basePath: string, @Optional() configuration: Configuration) {
    if (basePath) {
      this.basePath = basePath;
    }
    if (configuration) {
      this.configuration = configuration;
      this.basePath = basePath || configuration.basePath || this.basePath;
    }
  }

  /**
   * @param consumes string[] mime-types
   * @return true: consumes contains 'multipart/form-data', false: otherwise
   */
  private canConsumeForm(consumes: string[]): boolean {
    const form = 'multipart/form-data';
    for (const consume of consumes) {
      if (form === consume) {
        return true;
      }
    }
    return false;
  }


  /**
   *
   *
   * @param id
   * @param token
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public deleteSensorData(id: string, token: string, observe?: 'body', reportProgress?: boolean): Observable<string>;
  public deleteSensorData(id: string, token: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<string>>;
  public deleteSensorData(id: string, token: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<string>>;
  public deleteSensorData(id: string, token: string, observe: any = 'body', reportProgress: boolean = false): Observable<any> {

    if (id === null || id === undefined) {
      throw new Error('Required parameter id was null or undefined when calling deleteSensorData.');
    }

    if (token === null || token === undefined) {
      throw new Error('Required parameter token was null or undefined when calling deleteSensorData.');
    }

    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
      '*/*'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [];

    return this.httpClient.request<string>('delete', `${this.basePath}/v1/SensorData/${encodeURIComponent(String(id))}/${encodeURIComponent(String(token))}`,
      {
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }

  getProcessedSensorData(type: string): Observable<any> {
    return this.httpClient.get<any>(`${this.basePath}/v1/SensorData/processed/${type}`);
  }


  /**
   *
   *
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public getAllSensorBy10Min(observe?: 'body', reportProgress?: boolean): Observable<Array<SensorDataDto>>;
  public getAllSensorBy10Min(observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Array<SensorDataDto>>>;
  public getAllSensorBy10Min(observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Array<SensorDataDto>>>;
  public getAllSensorBy10Min(observe: any = 'body', reportProgress: boolean = false): Observable<any> {

    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
      '*/*'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [];

    return this.httpClient.request<Array<SensorDataDto>>('get', `${this.basePath}/v1/SensorData/latest`,
      {
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }

  /**
   *
   *
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public getAllSensorBy10MinByType(type: String, observe?: 'body', reportProgress?: boolean): Observable<Array<SensorDataDto>>;
  public getAllSensorBy10MinByType(type: String, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Array<SensorDataDto>>>;
  public getAllSensorBy10MinByType(type: String, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Array<SensorDataDto>>>;
  public getAllSensorBy10MinByType(type: String, observe: any = 'body', reportProgress: boolean = false): Observable<any> {

    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
      '*/*'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [];

    return this.httpClient.request<Array<SensorDataDto>>('get', `${this.basePath}/v1/SensorData/latest-by-type/${encodeURIComponent(String(type))}`,
      {
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }


  /**
   *
   *
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public getAllSensorData(observe?: 'body', reportProgress?: boolean): Observable<Array<SensorDataDto>>;
  public getAllSensorData(observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Array<SensorDataDto>>>;
  public getAllSensorData(observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Array<SensorDataDto>>>;
  public getAllSensorData(observe: any = 'body', reportProgress: boolean = false): Observable<any> {

    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
      '*/*'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [];

    return this.httpClient.request<Array<SensorDataDto>>('get', `${this.basePath}/v1/SensorData/get-all`,
      {
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }

  /**
   *
   *
   * @param id
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public getSensorDataById(id: string, observe?: 'body', reportProgress?: boolean): Observable<SensorDataDto>;
  public getSensorDataById(id: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SensorDataDto>>;
  public getSensorDataById(id: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SensorDataDto>>;
  public getSensorDataById(id: string, observe: any = 'body', reportProgress: boolean = false): Observable<any> {

    if (id === null || id === undefined) {
      throw new Error('Required parameter id was null or undefined when calling getSensorDataById.');
    }

    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
      '*/*'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [];

    return this.httpClient.request<SensorDataDto>('get', `${this.basePath}/v1/SensorData/${encodeURIComponent(String(id))}`,
      {
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }


  /**
   * Receive sensor data
   * Receive and save sensor data from micro-service
   * @param body
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public receiveSensorData(body: NewSensorDataDto, observe?: 'body', reportProgress?: boolean): Observable<SensorData>;
  public receiveSensorData(body: NewSensorDataDto, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SensorData>>;
  public receiveSensorData(body: NewSensorDataDto, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SensorData>>;
  public receiveSensorData(body: NewSensorDataDto, observe: any = 'body', reportProgress: boolean = false): Observable<any> {

    if (body === null || body === undefined) {
      throw new Error('Required parameter body was null or undefined when calling receiveSensorData.');
    }

    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
      '*/*'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [
      'application/json'
    ];
    const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
    if (httpContentTypeSelected != undefined) {
      headers = headers.set('Content-Type', httpContentTypeSelected);
    }

    return this.httpClient.request<SensorData>('post', `${this.basePath}/v1/sensorData`,
      {
        body: body,
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }

  /**
   * Save sensor data
   * Save new sensor data with optional file
   * @param data
   * @param file
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public saveSensorDataForm(data?: NewSensorDataDto, file?: Blob, observe?: 'body', reportProgress?: boolean): Observable<SensorData>;
  public saveSensorDataForm(data?: NewSensorDataDto, file?: Blob, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SensorData>>;
  public saveSensorDataForm(data?: NewSensorDataDto, file?: Blob, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SensorData>>;
  public saveSensorDataForm(data?: NewSensorDataDto, file?: Blob, observe: any = 'body', reportProgress: boolean = false): Observable<any> {


    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
      '*/*'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [
      'multipart/form-data'
    ];

    const canConsumeForm = this.canConsumeForm(consumes);

    let formParams: { append(param: string, value: any): void; };
    let useForm = false;
    let convertFormParamsToString = false;
    // use FormData to transmit files using content-type "multipart/form-data"
    // see https://stackoverflow.com/questions/4007969/application-x-www-form-urlencoded-or-multipart-form-data
    useForm = canConsumeForm;
    if (useForm) {
      formParams = new FormData();
    } else {
      formParams = new HttpParams({encoder: new CustomHttpUrlEncodingCodec()});
    }

    if (data !== undefined) {
      formParams = formParams.append('data', <any>data) as any || formParams;
    }
    if (file !== undefined) {
      formParams = formParams.append('file', <any>file) as any || formParams;
    }

    return this.httpClient.request<SensorData>('post', `${this.basePath}/v1/SaveSensorData`,
      {
        body: convertFormParamsToString ? formParams.toString() : formParams,
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }


  /**
   *
   *
   * @param body
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public saveSensorData(data: NewSensorDataDto, file: File, observe?: 'body', reportProgress?: boolean): Observable<any>;
  public saveSensorData(data: NewSensorDataDto, file: File, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<any>>;
  public saveSensorData(data: NewSensorDataDto, file: File, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<any>>;
  public saveSensorData(data: NewSensorDataDto, file: File, observe: any = 'body', reportProgress: boolean = false): Observable<any> {

    const dataBlob = new Blob([JSON.stringify(data)], {type: 'application/json'});
    const formData = new FormData();
    formData.append('data', dataBlob);
    formData.append('file', file);

    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
      '*/*'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [];
    const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
    if (httpContentTypeSelected != undefined) {
      headers = headers.set('Content-Type', httpContentTypeSelected);
    }

    return this.httpClient.post<SensorData>(`${this.basePath}/v1/SaveSensorData`, formData,

      {

        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }

  /**
   *
   *
   * @param body
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public updateSensorData(body: NewSensorDataDto, observe?: 'body', reportProgress?: boolean): Observable<SensorData>;
  public updateSensorData(body: NewSensorDataDto, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SensorData>>;
  public updateSensorData(body: NewSensorDataDto, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SensorData>>;
  public updateSensorData(body: NewSensorDataDto, observe: any = 'body', reportProgress: boolean = false): Observable<any> {

    if (body === null || body === undefined) {
      throw new Error('Required parameter body was null or undefined when calling updateSensorData.');
    }

    const id = body.Id

    let headers = this.defaultHeaders;

    // to determine the Accept header
    let httpHeaderAccepts: string[] = [
      '*/*'
    ];
    const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
    if (httpHeaderAcceptSelected != undefined) {
      headers = headers.set('Accept', httpHeaderAcceptSelected);
    }

    // to determine the Content-Type header
    const consumes: string[] = [
      'application/json'
    ];
    const httpContentTypeSelected: string | undefined = this.configuration.selectHeaderContentType(consumes);
    if (httpContentTypeSelected != undefined) {
      headers = headers.set('Content-Type', httpContentTypeSelected);
    }

    return this.httpClient.request<SensorData>('put', `${this.basePath}/v1/SensorData/${encodeURIComponent(String(id))}`,
      {
        body: body,
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }




}
