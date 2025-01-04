import {Component, OnInit} from '@angular/core';
import {SensorDataService} from "../../../../service/sensorData.service";
import {InterestAreaService} from "../../../../service/interestArea.service";
import {SensorService} from "../../../../service/sensor.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {CookieService} from "ngx-cookie-service";
import {ToolbarComponent} from "../../../elements/toolbar/toolbar.component";
import {ActivatedRoute, Router} from "@angular/router";
import {InterestAreaDto} from "../../../../model/interestAreaDto";
import {NgForOf} from "@angular/common";
import {InterestAreaDataService} from "../../../../service/InterestAreaDataService";

@Component({
  selector: 'app-interest-area-public-list',
  standalone: true,
  imports: [
    NgForOf
  ],
  templateUrl: './interest-area-public-list.component.html',
  styleUrl: './interest-area-public-list.component.css'
})
export class InterestAreaPublicListComponent implements OnInit {
  protected interestAreaList!: InterestAreaDto[];

  constructor(
    private interestAreaService: InterestAreaService,
    private interestAreaDataService: InterestAreaDataService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.interestAreaService.getAllPublicInterestArea().subscribe((area) => {
      this.interestAreaList = area;
    });
  }

  redirect(area: InterestAreaDto): void {
    this.interestAreaDataService.setArea(area);
    this.router.navigate(['/PublicInterestAreaView']);
  }
}




