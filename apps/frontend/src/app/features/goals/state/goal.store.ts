import { HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { ApiError } from '../../../core/problem-detail.interceptor';
import { GoalApi } from '../data/goal.api';
import { GoalSse, GoalStreamMessage } from '../data/goal.sse';
import { Goal } from '../models/goal.model';

@Injectable({ providedIn: 'root' })
export class GoalStore {
  private readonly api = inject(GoalApi);
  private readonly sse = inject(GoalSse);

  private readonly goalsSignal = signal<Goal[]>([]);
  private readonly loadingSignal = signal(false);
  private readonly errorSignal = signal<string | null>(null);
  private readonly completedGoalSignal = signal<Goal | null>(null);

  readonly goals = this.goalsSignal.asReadonly();
  readonly loading = this.loadingSignal.asReadonly();
  readonly error = this.errorSignal.asReadonly();
  readonly completedGoal = this.completedGoalSignal.asReadonly();

  constructor() {
    this.sse.messages$.subscribe((message) => this.applyStreamMessage(message));
  }

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
        this.replaceGoal(updated);
        this.loadingSignal.set(false);
      },
      error: (err: unknown) => this.fail(err),
    });
  }

  applyStreamMessage(message: GoalStreamMessage): void {
    this.replaceGoal(message.goal);
    if (message.type === 'goal-completed') {
      this.completedGoalSignal.set(message.goal);
    }
  }

  dismissCompleted(): void {
    this.completedGoalSignal.set(null);
  }

  private replaceGoal(updated: Goal): void {
    this.goalsSignal.update((goals) => {
      if (!goals.some((goal) => goal.id === updated.id)) {
        return [...goals, updated];
      }
      return goals.map((goal) => (goal.id === updated.id ? updated : goal));
    });
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
