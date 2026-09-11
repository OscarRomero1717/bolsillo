import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { environment } from '../../../environments/environment';
import { ContributionForm } from './components/contribution-form.component';
import { EVENT_SOURCE_FACTORY } from './data/goal.sse';
import { Goal } from './models/goal.model';
import { GoalDashboard } from './pages/goal-dashboard.component';
import { GoalStore } from './state/goal.store';

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

const silentEventSource = {
  provide: EVENT_SOURCE_FACTORY,
  useValue: (url: string) =>
    ({
      url,
      addEventListener: () => undefined,
      close: () => undefined,
    }) as unknown as EventSource,
};

describe('Enunciado — 4 tests UI', () => {
  describe('dashboard y dialogo', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [GoalDashboard],
        providers: [provideHttpClient(), provideHttpClientTesting(), silentEventSource],
      });
    });

    it('dashboard muestra las metas', async () => {
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

    it('tras contribute mock el porcentaje de la card cambia', async () => {
      const fixture = TestBed.createComponent(GoalDashboard);
      const http = TestBed.inject(HttpTestingController);
      fixture.detectChanges();
      http.expectOne(`${environment.apiUrl}/goals`).flush(seeds);
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();

      const contribution = fixture.debugElement.query(By.directive(ContributionForm));
      contribution.componentInstance.form.setValue({ amount: 400_000 });
      fixture.detectChanges();
      contribution.query(By.css('form')).triggerEventHandler('ngSubmit', {});

      const req = http.expectOne(`${environment.apiUrl}/goals/1/contributions`);
      expect(req.request.body).toEqual({ amount: 400_000 });
      req.flush({
        ...seeds[0],
        currentAmount: 400_000,
        progressPercent: 40,
        version: 1,
      });
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();

      expect((fixture.nativeElement as HTMLElement).textContent).toContain('40%');
      http.verify();
    });

    it('al emitir goal-completed el dialogo existe en el DOM', async () => {
      const fixture = TestBed.createComponent(GoalDashboard);
      const http = TestBed.inject(HttpTestingController);
      const store = TestBed.inject(GoalStore);
      fixture.detectChanges();
      http.expectOne(`${environment.apiUrl}/goals`).flush(seeds);
      fixture.detectChanges();
      await fixture.whenStable();

      expect((fixture.nativeElement as HTMLElement).querySelector('[role="dialog"]')).toBeNull();

      const completed: Goal = {
        ...seeds[1],
        currentAmount: 1_000_000,
        progressPercent: 100,
        status: 'COMPLETED',
        version: 2,
      };
      store.applyStreamMessage({ type: 'goal-completed', goal: completed });
      fixture.detectChanges();

      const dialog = (fixture.nativeElement as HTMLElement).querySelector('[role="dialog"]');
      expect(dialog).not.toBeNull();
      expect(dialog?.textContent).toContain('¡Meta cumplida!');
      expect(dialog?.textContent).toContain('Fondo emergencia');
      http.verify();
    });
  });

  describe('form abono', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ContributionForm],
        providers: [provideHttpClient(), provideHttpClientTesting()],
      });
    });

    it('form abono invalido (0 / negativo) no llama API', () => {
      const fixture = TestBed.createComponent(ContributionForm);
      fixture.componentRef.setInput('goalId', 'g1');
      fixture.componentRef.setInput('remaining', 1_000_000);
      const http = TestBed.inject(HttpTestingController);
      fixture.detectChanges();

      const form = fixture.debugElement.query(By.css('form'));
      fixture.componentInstance.form.setValue({ amount: 0 });
      fixture.detectChanges();
      form.triggerEventHandler('ngSubmit', {});

      fixture.componentInstance.form.setValue({ amount: -10 });
      fixture.detectChanges();
      form.triggerEventHandler('ngSubmit', {});

      http.expectNone(`${environment.apiUrl}/goals/g1/contributions`);
      http.verify();
    });
  });
});
