import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InterestAreaViewerComponent } from './interest-area-viewer.component';

describe('InterestAreaViewerComponent', () => {
  let component: InterestAreaViewerComponent;
  let fixture: ComponentFixture<InterestAreaViewerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InterestAreaViewerComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(InterestAreaViewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
