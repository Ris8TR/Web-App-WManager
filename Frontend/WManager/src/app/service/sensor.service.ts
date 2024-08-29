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

import { NewSensorDto } from '../model/newSensorDto';
import { SensorDto } from '../model/sensorDto';

import { BASE_PATH, COLLECTION_FORMATS }                     from '../variables';
import { Configuration }                                     from '../configuration';


@Injectable()
export class SensorService {

    protected basePath = 'http://192.168.15.34:8010';
    public defaultHeaders = new HttpHeaders();
    public configuration = new Configuration();

    constructor(protected httpClient: HttpClient, @Optional()@Inject(BASE_PATH) basePath: string, @Optional() configuration: Configuration) {
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
     * @param body
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public addSensor(body: NewSensorDto, observe?: 'body', reportProgress?: boolean): Observable<SensorDto>;
    public addSensor(body: NewSensorDto, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SensorDto>>;
    public addSensor(body: NewSensorDto, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SensorDto>>;
    public addSensor(body: NewSensorDto, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

        if (body === null || body === undefined) {
            throw new Error('Required parameter body was null or undefined when calling addSensor.');
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

        return this.httpClient.request<SensorDto>('post',`${this.basePath}/v1/sensors`,
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
   *
   *
   * @param companyName
   * @param token
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public findByCompanyName(companyName: string, token: string, observe?: 'body', reportProgress?: boolean): Observable<Array<SensorDto>>;
  public findByCompanyName(companyName: string, token: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Array<SensorDto>>>;
  public findByCompanyName(companyName: string, token: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Array<SensorDto>>>;
  public findByCompanyName(companyName: string, token: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

    if (companyName === null || companyName === undefined) {
      throw new Error('Required parameter companyName was null or undefined when calling findByCompanyName.');
    }

    if (token === null || token === undefined) {
      throw new Error('Required parameter token was null or undefined when calling findByCompanyName.');
    }

    let queryParameters = new HttpParams({encoder: new CustomHttpUrlEncodingCodec()});
    if (token !== undefined && token !== null) {
      queryParameters = queryParameters.set('token', <any>token);
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
    ];

    return this.httpClient.request<Array<SensorDto>>('get',`${this.basePath}/v1/sensors/${encodeURIComponent(String(companyName))}/${encodeURIComponent(String(token))}`,
      {
        params: queryParameters,
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
   * @param token
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public findById(id: string, token: string, observe?: 'body', reportProgress?: boolean): Observable<SensorDto>;
  public findById(id: string, token: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SensorDto>>;
  public findById(id: string, token: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SensorDto>>;
  public findById(id: string, token: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

    if (id === null || id === undefined) {
      throw new Error('Required parameter id was null or undefined when calling findById.');
    }

    if (token === null || token === undefined) {
      throw new Error('Required parameter token was null or undefined when calling findById.');
    }

    let queryParameters = new HttpParams({encoder: new CustomHttpUrlEncodingCodec()});
    if (token !== undefined && token !== null) {
      queryParameters = queryParameters.set('token', <any>token);
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
    ];

    return this.httpClient.request<SensorDto>('get',`${this.basePath}/v1/sensors/${encodeURIComponent(String(id))}/${encodeURIComponent(String(token))}`,
      {
        params: queryParameters,
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
   * @param type
   * @param token
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public findByTypeAndUserId(type: string, token: string, observe?: 'body', reportProgress?: boolean): Observable<Array<SensorDto>>;
  public findByTypeAndUserId(type: string, token: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Array<SensorDto>>>;
  public findByTypeAndUserId(type: string, token: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Array<SensorDto>>>;
  public findByTypeAndUserId(type: string, token: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

    if (type === null || type === undefined) {
      throw new Error('Required parameter type was null or undefined when calling findByTypeAndUserId.');
    }

    if (token === null || token === undefined) {
      throw new Error('Required parameter token was null or undefined when calling findByTypeAndUserId.');
    }

    let queryParameters = new HttpParams({encoder: new CustomHttpUrlEncodingCodec()});
    if (token !== undefined && token !== null) {
      queryParameters = queryParameters.set('token', <any>token);
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
    ];

    return this.httpClient.request<Array<SensorDto>>('get',`${this.basePath}/v1/sensors/type/${encodeURIComponent(String(type))}/${encodeURIComponent(String(token))}`,
      {
        params: queryParameters,
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
   * @param token
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public findByUserId(token: string, observe?: 'body', reportProgress?: boolean): Observable<Array<SensorDto>>;
  public findByUserId(token: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Array<SensorDto>>>;
  public findByUserId(token: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Array<SensorDto>>>;
  public findByUserId(token: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

    if (token === null || token === undefined) {
      throw new Error('Required parameter token was null or undefined when calling findByUserId.');
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
    ];

    return this.httpClient.request<Array<SensorDto>>('get',`${this.basePath}/v1/sensors/user/${encodeURIComponent(String(token))}`,
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
  public getAllSensor(observe?: 'body', reportProgress?: boolean): Observable<Array<SensorDto>>;
  public getAllSensor(observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Array<SensorDto>>>;
  public getAllSensor(observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Array<SensorDto>>>;
  public getAllSensor(observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

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
    ];

    return this.httpClient.request<Array<SensorDto>>('get',`${this.basePath}/v1/sensors/all-sensors`,
      {
        withCredentials: this.configuration.withCredentials,
        headers: headers,
        observe: observe,
        reportProgress: reportProgress
      }
    );
  }

    /**
     * Save sensor
     * Save sensor with file
     * @param file
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public newSensorForm(file?: Blob, observe?: 'body', reportProgress?: boolean): Observable<SensorDto>;
    public newSensorForm(file?: Blob, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<SensorDto>>;
    public newSensorForm(file?: Blob, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<SensorDto>>;
    public newSensorForm(file?: Blob, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {


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

        if (file !== undefined) {
            formParams = formParams.append('file', <any>file) as any || formParams;
        }

        return this.httpClient.request<SensorDto>('post',`${this.basePath}/v1/new-sensors`,
            {
                body: convertFormParamsToString ? formParams.toString() : formParams,
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

}
