import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
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

  it('opens when the stream reports a completed goal, then closes on Cerrar', () => {
    const fixture = TestBed.createComponent(GoalCompletedDialog);
    const store = TestBed.inject(GoalStore);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelector('[role="dialog"]')).toBeNull();

    store.applyStreamMessage({ type: 'goal-completed', goal: completed });
    fixture.detectChanges();

    const host = fixture.nativeElement as HTMLElement;
    expect(host.textContent).toContain('¡Meta cumplida!');
    expect(host.textContent).toContain('Viaje');
    expect(host.querySelector('[role="dialog"]')).not.toBeNull();

    host.querySelector('button')?.click();
    fixture.detectChanges();

    expect(host.querySelector('[role="dialog"]')).toBeNull();
    expect(store.completedGoal()).toBeNull();
  });
});
