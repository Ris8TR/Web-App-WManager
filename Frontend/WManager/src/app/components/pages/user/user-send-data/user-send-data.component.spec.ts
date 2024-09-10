import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserSendDataComponent } from './user-send-data.component';

describe('UserSendDataComponent', () => {
  let component: UserSendDataComponent;
  let fixture: ComponentFixture<UserSendDataComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserSendDataComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UserSendDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
