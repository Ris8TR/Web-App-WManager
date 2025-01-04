import {InterestAreaDto} from "../model/interestAreaDto";
import {BehaviorSubject} from "rxjs";
import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root',
})
export class InterestAreaDataService {
  private readonly storageKey = 'selectedInterestArea';
  private areaSource = new BehaviorSubject<InterestAreaDto | null>(this.getStoredArea());
  currentArea = this.areaSource.asObservable();

  setArea(area: InterestAreaDto): void {
    this.areaSource.next(area);
    localStorage.setItem(this.storageKey, JSON.stringify(area));
  }

  private getStoredArea(): InterestAreaDto | null {
    const storedArea = localStorage.getItem(this.storageKey);
    return storedArea ? JSON.parse(storedArea) : null;
  }
}
