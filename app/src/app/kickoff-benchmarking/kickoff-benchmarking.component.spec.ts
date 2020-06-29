import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { KickoffBenchmarkingComponent } from './kickoff-benchmarking.component';

describe('KickoffBenchmarkingComponent', () => {
  let component: KickoffBenchmarkingComponent;
  let fixture: ComponentFixture<KickoffBenchmarkingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ KickoffBenchmarkingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(KickoffBenchmarkingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
