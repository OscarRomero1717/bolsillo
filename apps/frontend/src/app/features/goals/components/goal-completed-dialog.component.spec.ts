import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { Goal } from '../models/goal.model';
import { GoalStore } from '../state/goal.store';
import { GoalCompletedDialog } from './goal-completed-dialog.component';

describe('GoalCompletedDialog', () => {
  const completed: Goal = {
    id: 'g1',
    name: 'Viaje',
    targetAmount: 100,
    currentAmount: 100,
    progressPercent: 100,
    status: 'COMPLETED',
    version: 2,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [GoalCompletedDialog],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('stays closed until a contribution completes a goal, then closes on Cerrar', () => {
    const fixture = TestBed.createComponent(GoalCompletedDialog);
    const store = TestBed.inject(GoalStore);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelector('[role="dialog"]')).toBeNull();

    store.contribute('g1', 60);
    http.expectOne(`${environment.apiUrl}/goals/g1/contributions`).flush(completed);
    fixture.detectChanges();

    const host = fixture.nativeElement as HTMLElement;
    expect(host.textContent).toContain('¡Meta cumplida!');
    expect(host.textContent).toContain('Viaje');
    expect(host.querySelector('[role="dialog"]')).not.toBeNull();

    host.querySelector('button')?.click();
    fixture.detectChanges();

    expect(host.querySelector('[role="dialog"]')).toBeNull();
    expect(store.completedGoal()).toBeNull();
    http.verify();
  });
});
