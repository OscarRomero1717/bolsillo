import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { Goal } from '../models/goal.model';
import { GoalStore } from './goal.store';

describe('GoalStore', () => {
  let store: GoalStore;
  let http: HttpTestingController;

  const openGoal: Goal = {
    id: 'g1',
    name: 'Viaje',
    targetAmount: 100,
    currentAmount: 40,
    progressPercent: 40,
    status: 'OPEN',
    version: 1,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    store = TestBed.inject(GoalStore);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('load fills goals signal', () => {
    store.load();
    http.expectOne(`${environment.apiUrl}/goals`).flush([openGoal]);

    expect(store.goals()).toEqual([openGoal]);
    expect(store.loading()).toBe(false);
    expect(store.error()).toBeNull();
  });

  it('contribute replaces the matching goal in the list', () => {
    store.load();
    http.expectOne(`${environment.apiUrl}/goals`).flush([openGoal]);

    const updated: Goal = { ...openGoal, currentAmount: 70, progressPercent: 70, version: 2 };
    store.contribute('g1', 30);
    http.expectOne(`${environment.apiUrl}/goals/g1/contributions`).flush(updated);

    expect(store.goals()).toEqual([updated]);
    expect(store.completedGoal()).toBeNull();
  });

  it('contribute sets completedGoal when status is COMPLETED', () => {
    store.load();
    http.expectOne(`${environment.apiUrl}/goals`).flush([openGoal]);

    const completed: Goal = {
      ...openGoal,
      currentAmount: 100,
      progressPercent: 100,
      status: 'COMPLETED',
      version: 2,
    };
    store.contribute('g1', 60);
    http.expectOne(`${environment.apiUrl}/goals/g1/contributions`).flush(completed);

    expect(store.goals()[0].status).toBe('COMPLETED');
    expect(store.completedGoal()).toEqual(completed);
  });

  it('dismissCompleted clears the completedGoal signal', () => {
    store.load();
    http.expectOne(`${environment.apiUrl}/goals`).flush([openGoal]);

    const completed: Goal = {
      ...openGoal,
      currentAmount: 100,
      progressPercent: 100,
      status: 'COMPLETED',
      version: 2,
    };
    store.contribute('g1', 60);
    http.expectOne(`${environment.apiUrl}/goals/g1/contributions`).flush(completed);

    store.dismissCompleted();
    expect(store.completedGoal()).toBeNull();
  });
});
