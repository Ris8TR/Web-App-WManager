import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsermodifyComponent } from './usermodify.component';

describe('UsermodifyComponent', () => {
  let component: UsermodifyComponent;
  let fixture: ComponentFixture<UsermodifyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsermodifyComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UsermodifyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
