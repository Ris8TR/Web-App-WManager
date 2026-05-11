import {Observable} from "rxjs";
import {UserPreferenceDto} from "../model/userPreferenceDto";
import {NewUserPreferenceDto} from "../model/newUserPreferenceDto";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Configuration} from "../configuration";
import {Inject, Injectable, Optional} from "@angular/core";
import {BASE_PATH} from "../variables";

@Injectable()
export class UserPreferenceService {
  protected basePath = 'http://192.168.15.34:8010';
  public defaultHeaders = new HttpHeaders();
  public configuration = new Configuration();

  constructor(protected httpClient: HttpClient, @Optional()@Inject(BASE_PATH) basePath: string, @Optional() configuration: Configuration) {
    if (basePath) { this.basePath = basePath; }
    if (configuration) {
      this.configuration = configuration;
      this.basePath = basePath || configuration.basePath || this.basePath;
    }
  }

  // Helper per aggiungere l'header di autorizzazione
  private getHeaders(authHeader: string): HttpHeaders {
    return this.defaultHeaders.set('Authorization', authHeader);
  }

  public deleteUserPreference(authHeader: string, userId: string): Observable<any> {
    return this.httpClient.request<any>('delete', `${this.basePath}/v1/UserPreference/${encodeURIComponent(String(userId))}`, {
      headers: this.getHeaders(authHeader),
      withCredentials: this.configuration.withCredentials
    });
  }

  public getAllUserPreferences(authHeader: string): Observable<Array<UserPreferenceDto>> {
    return this.httpClient.get<Array<UserPreferenceDto>>(`${this.basePath}/v1/UserPreference/getAll`, {
      headers: this.getHeaders(authHeader),
      withCredentials: this.configuration.withCredentials
    });
  }

  public getUserPreferenceByUserId(authHeader: string): Observable<UserPreferenceDto> {
    return this.httpClient.get<UserPreferenceDto>(`${this.basePath}/v1/UserPreference/user`, {
      headers: this.getHeaders(authHeader),
      withCredentials: this.configuration.withCredentials
    });
  }

  public saveUserPreference(authHeader: string, body: NewUserPreferenceDto): Observable<UserPreferenceDto> {
    return this.httpClient.post<UserPreferenceDto>(`${this.basePath}/v1/UserPreference`, body, {
      headers: this.getHeaders(authHeader).set('Content-Type', 'application/json'),
      withCredentials: this.configuration.withCredentials
    });
  }

  public updateUserPreference(authHeader: string, userId: string, body: UserPreferenceDto): Observable<UserPreferenceDto> {
    return this.httpClient.put<UserPreferenceDto>(`${this.basePath}/v1/UserPreference/${encodeURIComponent(String(userId))}`, body, {
      headers: this.getHeaders(authHeader).set('Content-Type', 'application/json'),
      withCredentials: this.configuration.withCredentials
    });
  }
}
