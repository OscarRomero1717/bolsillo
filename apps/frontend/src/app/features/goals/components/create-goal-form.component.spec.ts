import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { Goal } from '../models/goal.model';
import { CreateGoalForm } from './create-goal-form.component';

describe('CreateGoalForm', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [CreateGoalForm],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('disables submit while name or target are invalid', () => {
    const fixture = TestBed.createComponent(CreateGoalForm);
    fixture.detectChanges();
    const button = (fixture.nativeElement as HTMLElement).querySelector('button');
    expect(button?.hasAttribute('disabled')).toBe(true);

    fixture.componentInstance.form.setValue({ name: 'Viaje', targetAmount: 1_000_000 });
    fixture.detectChanges();
    expect(button?.hasAttribute('disabled')).toBe(false);
  });

  it('posts the new goal and then clears the form', () => {
    const fixture = TestBed.createComponent(CreateGoalForm);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    fixture.componentInstance.form.setValue({ name: 'Viaje', targetAmount: 1_000_000 });
    fixture.detectChanges();
    (fixture.nativeElement as HTMLElement).querySelector('button')?.click();

    const req = http.expectOne(`${environment.apiUrl}/goals`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'Viaje', targetAmount: 1_000_000 });

    const created: Goal = {
      id: 'new-1',
      name: 'Viaje',
      targetAmount: 1_000_000,
      currentAmount: 0,
      progressPercent: 0,
      status: 'OPEN',
      version: 0,
    };
    req.flush(created);
    fixture.detectChanges();

    expect(fixture.componentInstance.form.value).toEqual({ name: '', targetAmount: null });
    http.verify();
  });
});
