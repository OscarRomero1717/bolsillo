import { HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApiError, problemDetailInterceptor } from './problem-detail.interceptor';
import { GoalApi } from '../features/goals/data/goal.api';
import { environment } from '../../environments/environment';

describe('problemDetailInterceptor', () => {
  let api: GoalApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([problemDetailInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    api = TestBed.inject(GoalApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('maps 400 Problem Details to ApiError with detail and code', () => {
    let captured: unknown;
    api.contribute('goal-1', { amount: 0 }).subscribe({
      next: () => {
        throw new Error('expected error');
      },
      error: (err: unknown) => {
        captured = err;
      },
    });

    http.expectOne(`${environment.apiUrl}/goals/goal-1/contributions`).flush(
      {
        title: 'Validation failed',
        status: 400,
        detail: 'amount: must be greater than 0',
        code: 'VALIDATION_ERROR',
      },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(captured).toBeInstanceOf(ApiError);
    const apiError = captured as ApiError;
    expect(apiError.status).toBe(400);
    expect(apiError.detail).toBe('amount: must be greater than 0');
    expect(apiError.code).toBe('VALIDATION_ERROR');
  });

  it('leaves 404 as HttpErrorResponse', () => {
    let captured: unknown;
    api.list().subscribe({
      next: () => {
        throw new Error('expected error');
      },
      error: (err: unknown) => {
        captured = err;
      },
    });

    http.expectOne(`${environment.apiUrl}/goals`).flush(
      { status: 404, code: 'GOAL_NOT_FOUND' },
      { status: 404, statusText: 'Not Found' },
    );

    expect(captured).toBeInstanceOf(HttpErrorResponse);
  });
});
