import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { Goal } from '../models/goal.model';
import { GoalApi } from './goal.api';

describe('GoalApi', () => {
  let api: GoalApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(GoalApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('list GETs /api/goals', () => {
    const body: Goal[] = [
      {
        id: '1',
        name: 'Viaje a Cartagena',
        targetAmount: 1_000_000,
        currentAmount: 0,
        progressPercent: 0,
        status: 'OPEN',
        version: 0,
      },
    ];

    api.list().subscribe((goals) => {
      expect(goals.length).toBe(1);
      expect(goals[0].name).toBe('Viaje a Cartagena');
    });

    const req = http.expectOne(`${environment.apiUrl}/goals`);
    expect(req.request.method).toBe('GET');
    req.flush(body);
  });
});
