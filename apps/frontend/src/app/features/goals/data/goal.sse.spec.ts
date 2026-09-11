import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { EVENT_SOURCE_FACTORY, GoalSse } from './goal.sse';

class FakeEventSource {
  readonly listeners = new Map<string, Array<(event: Event) => void>>();
  closed = false;

  constructor(readonly url: string) {}

  addEventListener(type: string, listener: EventListenerOrEventListenerObject): void {
    const list = this.listeners.get(type) ?? [];
    list.push(listener as (event: Event) => void);
    this.listeners.set(type, list);
  }

  close(): void {
    this.closed = true;
  }

  emit(type: string, data: string): void {
    const event = new MessageEvent('message', { data });
    for (const listener of this.listeners.get(type) ?? []) {
      listener(event);
    }
  }
}

describe('GoalSse', () => {
  let sse: GoalSse;
  let source: FakeEventSource;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: EVENT_SOURCE_FACTORY,
          useValue: (url: string) => {
            source = new FakeEventSource(url);
            return source as unknown as EventSource;
          },
        },
      ],
    });
    sse = TestBed.inject(GoalSse);
  });

  it('connects to /api/goals/stream once', () => {
    sse.connect();
    sse.connect();

    expect(source.url).toBe(`${environment.apiUrl}/goals/stream`);
  });

  it('parses named events without any', () => {
    const seen: string[] = [];
    sse.messages$.subscribe((message) => seen.push(message.type));
    sse.connect();

    const goalJson = JSON.stringify({
      id: 'g1',
      name: 'Viaje',
      targetAmount: 100,
      currentAmount: 100,
      progressPercent: 100,
      status: 'COMPLETED',
      version: 2,
    });
    source.emit('goal-updated', goalJson);
    source.emit('goal-completed', goalJson);

    expect(seen).toEqual(['goal-updated', 'goal-completed']);
  });

  it('disconnect closes the EventSource', () => {
    sse.connect();
    sse.disconnect();
    expect(source.closed).toBe(true);
  });
});
