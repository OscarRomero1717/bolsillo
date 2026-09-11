import { Injectable, InjectionToken, inject } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Goal, GoalStatus } from '../models/goal.model';

export const EVENT_SOURCE_FACTORY = new InjectionToken<(url: string) => EventSource>(
  'EventSourceFactory',
  {
    providedIn: 'root',
    factory: () => (url: string) => new EventSource(url),
  },
);

export type GoalUpdated = {
  type: 'goal-updated';
  goal: Goal;
};

export type GoalCompleted = {
  type: 'goal-completed';
  goal: Goal;
};

export type GoalStreamMessage = GoalUpdated | GoalCompleted;

@Injectable({ providedIn: 'root' })
export class GoalSse {
  private readonly createSource = inject(EVENT_SOURCE_FACTORY);
  private source: EventSource | null = null;
  private readonly messagesSubject = new Subject<GoalStreamMessage>();

  readonly messages$: Observable<GoalStreamMessage> = this.messagesSubject.asObservable();

  connect(): void {
    if (this.source !== null) {
      return;
    }
    this.source = this.createSource(`${environment.apiUrl}/goals/stream`);
    this.source.addEventListener('goal-updated', (event) => this.handle('goal-updated', event));
    this.source.addEventListener('goal-completed', (event) => this.handle('goal-completed', event));
  }

  disconnect(): void {
    this.source?.close();
    this.source = null;
  }

  private handle(type: GoalStreamMessage['type'], event: Event): void {
    if (!isMessageEvent(event) || typeof event.data !== 'string') {
      return;
    }
    try {
      this.messagesSubject.next({ type, goal: parseGoal(event.data) });
    } catch {
      // Malformed payload must not break the open stream.
    }
  }
}

function isMessageEvent(event: Event): event is MessageEvent<string> {
  return typeof MessageEvent !== 'undefined' && event instanceof MessageEvent;
}

function parseGoal(raw: string): Goal {
  const data: unknown = JSON.parse(raw);
  if (!isGoal(data)) {
    throw new Error('Invalid goal payload');
  }
  return data;
}

function isGoal(value: unknown): value is Goal {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const fields = value as Record<string, unknown>;
  return (
    typeof fields['id'] === 'string' &&
    typeof fields['name'] === 'string' &&
    typeof fields['targetAmount'] === 'number' &&
    typeof fields['currentAmount'] === 'number' &&
    typeof fields['progressPercent'] === 'number' &&
    isGoalStatus(fields['status']) &&
    typeof fields['version'] === 'number'
  );
}

function isGoalStatus(value: unknown): value is GoalStatus {
  return value === 'OPEN' || value === 'COMPLETED';
}
