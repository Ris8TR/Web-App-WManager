import { Component, Input, input } from '@angular/core';

@Component({
  selector: 'app-detailed-area',
  standalone: true,
  imports: [],
  templateUrl: './detailed-area.component.html',
  styleUrl: './detailed-area.component.css'
})
export class DetailedAreaComponent {

  @Input() interestAreaId: any;

}
