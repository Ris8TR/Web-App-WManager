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
import { HttpClient, HttpHeaders, HttpParams, HttpResponse, HttpEvent }                           from '@angular/common/http';
import { CustomHttpUrlEncodingCodec }                        from '../encoder';

import {BehaviorSubject, Observable} from 'rxjs';

import { InterestArea } from '../model/interestArea';
import { NewInterestAreaDto } from '../model/newInterestAreaDto';
import { ObjectId } from '../model/objectId';
import { SensorDataDto } from '../model/sensorDataDto';
import { ServiceError } from '../model/serviceError';

import { BASE_PATH, COLLECTION_FORMATS }                     from '../variables';
import { Configuration }                                     from '../configuration';
import { InterestAreaDto } from '../model/interestAreaDto';
import {CookieService} from "ngx-cookie-service";
import {ToolbarComponent} from "../components/elements/toolbar/toolbar.component";
import {SensorDto} from "../model/sensorDto";


@Injectable()
export class InterestAreaService {

    protected basePath = 'http://192.168.15.34:8010';
    public defaultHeaders = new HttpHeaders();
    public configuration = new Configuration();
    private idSubject = new BehaviorSubject<string | null>(this.getStoredId());
      currentId$ = this.idSubject.asObservable();

      // Metodo per aggiornare l'ID
      setId(id: string) {
        this.idSubject.next(id); // Emissione del nuovo ID
        localStorage.setItem('interestAreaId', id); // Salva l'ID in localStorage
      }

      // Recupera l'ID da localStorage all'avvio dell'app
      private getStoredId(): string | null {
        return localStorage.getItem('interestAreaId');
      }


    constructor(protected httpClient: HttpClient, @Optional()@Inject(BASE_PATH) basePath: string, @Optional() configuration: Configuration, private CookiesService: CookieService,
    ) {
        if (basePath) {
            this.basePath = basePath;
        }
        if (configuration) {
            this.configuration = configuration;
            this.basePath = basePath || configuration.basePath || this.basePath;
        }
    }



  /**
   *
   *
   * @param data
   * @param file
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public createInterestAreaForm(data?: NewInterestAreaDto, file?: Blob, observe?: 'body', reportProgress?: boolean): Observable<InterestAreaDto>;
  public createInterestAreaForm(data?: NewInterestAreaDto, file?: Blob, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<InterestAreaDto>>;
  public createInterestAreaForm(data?: NewInterestAreaDto, file?: Blob, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<InterestAreaDto>>;
  public createInterestAreaForm(data?: NewInterestAreaDto, file?: Blob, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

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

    console.log('Authorization Header:', headers.get('Authorization'));
    console.log('Accept Header:', headers.get('Accept'));
    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' })); // Assuming data is JSON
    formData.append('file', file!); // Assuming file is a Blob or File object

    return this.httpClient.post<InterestAreaDto>(`${this.basePath}/v1/interestArea`, formData, {
      withCredentials: this.configuration.withCredentials,
      headers: headers, // headers should not include Content-Type
      observe: observe,
      reportProgress: reportProgress
    });
  }


  private selectHeaderAccept(accepts: string[]): string | undefined {
    if (accepts.length === 0) {
      return undefined;
    }
    const type = accepts.find(x => x === 'application/json');
    if (type) {
      return type;
    }
    return accepts[0];
  }

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
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public deleteInterestArea(id: ObjectId, observe?: 'body', reportProgress?: boolean): Observable<any>;
  public deleteInterestArea(id: ObjectId, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<any>>;
  public deleteInterestArea(id: ObjectId, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<any>>;
  public deleteInterestArea(id: ObjectId, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

    if (id === null || id === undefined) {
      throw new Error('Required parameter id was null or undefined when calling deleteInterestArea.');
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

    return this.httpClient.request<any>('delete',`${this.basePath}/v1/interestarea/${encodeURIComponent(String(id))}`,
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
   * @param token
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public getInterestArea(id: string | undefined, token: string, observe?: 'body', reportProgress?: boolean): Observable<InterestArea>;
  public getInterestArea(id: string, token: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<InterestArea>>;
  public getInterestArea(id: string, token: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<InterestArea>>;
  public getInterestArea(id: string, token: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

    if (id === null || id === undefined) {
      throw new Error('Required parameter id was null or undefined when calling getInterestArea.');
    }

    if (token === null || token === undefined) {
      throw new Error('Required parameter token was null or undefined when calling getInterestArea.');
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

    return this.httpClient.request<InterestArea>('get',`${this.basePath}/v1/interestarea/${encodeURIComponent(String(id))}/${encodeURIComponent(String(token))}`,
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
     * @param token
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public getInterestAreasByUser(token: string, observe?: 'body', reportProgress?: boolean): Observable<Array<InterestAreaDto>>;
    public getInterestAreasByUser(token: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Array<InterestAreaDto>>>;
    public getInterestAreasByUser(token: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Array<InterestAreaDto>>>;
    public getInterestAreasByUser(token: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

        if (token === null || token === undefined) {
            throw new Error('Required parameter token was null or undefined when calling getInterestAreasByUser.');
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

        return this.httpClient.request<Array<InterestAreaDto>>('get',`${this.basePath}/v1/interestarea`,
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
   * @param interestAreaId
   * @param token
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  public getLatestSensorDataInInterestArea(interestAreaId: string, token: string, observe?: 'body', reportProgress?: boolean): Observable<Array<SensorDataDto>>;
  public getLatestSensorDataInInterestArea(interestAreaId: string, token: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<Array<SensorDataDto>>>;
  public getLatestSensorDataInInterestArea(interestAreaId: string, token: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<Array<SensorDataDto>>>;
  public getLatestSensorDataInInterestArea(interestAreaId: string, token: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {
    if (interestAreaId === null || interestAreaId === undefined) {
      throw new Error('Required parameter interestAreaId was null or undefined when calling getLatestSensorDataInInterestArea.');
    }

    if (token === null || token === undefined) {
      throw new Error('Required parameter token was null or undefined when calling getLatestSensorDataInInterestArea.');
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

    return this.httpClient.request<Array<SensorDataDto>>('get',`${this.basePath}/v1/${encodeURIComponent(String(interestAreaId))}/latest-sensor-data/${encodeURIComponent(String(token))}`,
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
  public updateArea(body: InterestAreaDto, observe?: 'body', reportProgress?: boolean): Observable<InterestAreaDto>;
  public updateArea(body: InterestAreaDto, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<InterestAreaDto>>;
  public updateArea(body: InterestAreaDto, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<InterestAreaDto>>;
  public updateArea(body: InterestAreaDto, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

    if (body === null || body === undefined) {
      throw new Error('Required parameter body was null or undefined when calling updateSensor.');
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


    return this.httpClient.request<SensorDto>('put',`${this.basePath}/v1/interestarea/update`,
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
