import { HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { ApiError } from '../../../core/problem-detail.interceptor';
import { GoalApi } from '../data/goal.api';
import { Goal } from '../models/goal.model';

@Injectable({ providedIn: 'root' })
export class GoalStore {
  private readonly api = inject(GoalApi);

  private readonly goalsSignal = signal<Goal[]>([]);
  private readonly loadingSignal = signal(false);
  private readonly errorSignal = signal<string | null>(null);
  private readonly completedGoalSignal = signal<Goal | null>(null);

  readonly goals = this.goalsSignal.asReadonly();
  readonly loading = this.loadingSignal.asReadonly();
  readonly error = this.errorSignal.asReadonly();
  readonly completedGoal = this.completedGoalSignal.asReadonly();

  load(): void {
    this.loadingSignal.set(true);
    this.errorSignal.set(null);
    this.api.list().subscribe({
      next: (goals) => {
        this.goalsSignal.set(goals);
        this.loadingSignal.set(false);
      },
      error: (err: unknown) => this.fail(err),
    });
  }

  create(name: string, target: number): void {
    this.loadingSignal.set(true);
    this.errorSignal.set(null);
    this.api.create({ name, targetAmount: target }).subscribe({
      next: (created) => {
        this.goalsSignal.update((goals) => [...goals, created]);
        this.loadingSignal.set(false);
      },
      error: (err: unknown) => this.fail(err),
    });
  }

  contribute(id: string, amount: number): void {
    this.loadingSignal.set(true);
    this.errorSignal.set(null);
    this.api.contribute(id, { amount }).subscribe({
      next: (updated) => {
        this.goalsSignal.update((goals) =>
          goals.map((goal) => (goal.id === id ? updated : goal)),
        );
        if (updated.status === 'COMPLETED') {
          this.completedGoalSignal.set(updated);
        }
        this.loadingSignal.set(false);
      },
      error: (err: unknown) => this.fail(err),
    });
  }

  dismissCompleted(): void {
    this.completedGoalSignal.set(null);
  }

  private fail(err: unknown): void {
    this.errorSignal.set(messageFrom(err));
    this.loadingSignal.set(false);
  }
}

function messageFrom(err: unknown): string {
  if (err instanceof ApiError) {
    return err.code ? `${err.code}: ${err.detail}` : err.detail;
  }
  if (err instanceof HttpErrorResponse && err.status === 0) {
    return 'No se pudo conectar con el backend';
  }
  return 'Error inesperado';
}
