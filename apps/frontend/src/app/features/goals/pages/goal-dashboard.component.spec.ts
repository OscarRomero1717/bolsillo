import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { Goal } from '../models/goal.model';
import { GoalDashboard } from './goal-dashboard.component';

describe('GoalDashboard', () => {
  const seeds: Goal[] = [
    {
      id: '1',
      name: 'Viaje a Cartagena',
      targetAmount: 1_000_000,
      currentAmount: 0,
      progressPercent: 0,
      status: 'OPEN',
      version: 0,
    },
    {
      id: '2',
      name: 'Fondo emergencia',
      targetAmount: 1_000_000,
      currentAmount: 900_000,
      progressPercent: 90,
      status: 'OPEN',
      version: 0,
    },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [GoalDashboard],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('renders seed goals after load', async () => {
    const fixture = TestBed.createComponent(GoalDashboard);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(`${environment.apiUrl}/goals`).flush(seeds);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Viaje a Cartagena');
    expect(text).toContain('Fondo emergencia');
    http.verify();
  });

  it('shows empty message when there are no goals', async () => {
    const fixture = TestBed.createComponent(GoalDashboard);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(`${environment.apiUrl}/goals`).flush([]);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('no hay metas');
    http.verify();
  });
});
