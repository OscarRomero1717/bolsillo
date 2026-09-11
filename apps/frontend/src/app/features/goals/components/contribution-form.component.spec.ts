import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { problemDetailInterceptor } from '../../../core/problem-detail.interceptor';
import { environment } from '../../../../environments/environment';
import { Goal } from '../models/goal.model';
import { ContributionForm } from './contribution-form.component';

describe('ContributionForm', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ContributionForm],
      providers: [
        provideHttpClient(withInterceptors([problemDetailInterceptor])),
        provideHttpClientTesting(),
      ],
    });
  });

  it('disables submit while amount is invalid', () => {
    const fixture = TestBed.createComponent(ContributionForm);
    fixture.componentRef.setInput('goalId', 'g1');
    fixture.componentRef.setInput('remaining', 1_000_000);
    fixture.detectChanges();
    const button = (fixture.nativeElement as HTMLElement).querySelector('button');
    expect(button?.hasAttribute('disabled')).toBe(true);

    fixture.componentInstance.form.setValue({ amount: 200_000 });
    fixture.detectChanges();
    expect(button?.hasAttribute('disabled')).toBe(false);
  });

  it('posts a contribution', () => {
    const fixture = TestBed.createComponent(ContributionForm);
    fixture.componentRef.setInput('goalId', 'g1');
    fixture.componentRef.setInput('remaining', 1_000_000);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    fixture.componentInstance.form.setValue({ amount: 200_000 });
    fixture.detectChanges();
    (fixture.nativeElement as HTMLElement).querySelector('button')?.click();

    const req = http.expectOne(`${environment.apiUrl}/goals/g1/contributions`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ amount: 200_000 });

    const updated: Goal = {
      id: 'g1',
      name: 'Viaje',
      targetAmount: 1_000_000,
      currentAmount: 200_000,
      progressPercent: 20,
      status: 'OPEN',
      version: 1,
    };
    req.flush(updated);
    http.verify();
  });

  it('shows a 422 Problem Detail under the input', () => {
    const fixture = TestBed.createComponent(ContributionForm);
    fixture.componentRef.setInput('goalId', 'g1');
    fixture.componentRef.setInput('remaining', 10_000_000);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    fixture.componentInstance.form.setValue({ amount: 9_000_000 });
    fixture.detectChanges();
    (fixture.nativeElement as HTMLElement).querySelector('button')?.click();

    http.expectOne(`${environment.apiUrl}/goals/g1/contributions`).flush(
      {
        title: 'Contribution not allowed',
        status: 422,
        detail: 'Contribution exceeds remaining amount',
        code: 'CONTRIBUTION_EXCEEDS_REMAINING',
      },
      { status: 422, statusText: 'Unprocessable Entity' },
    );
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'CONTRIBUTION_EXCEEDS_REMAINING: Contribution exceeds remaining amount',
    );
    http.verify();
  });
});
