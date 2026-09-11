import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { GoalCard } from '../components/goal-card.component';
import { CreateGoalForm } from '../components/create-goal-form.component';
import { GoalCompletedDialog } from '../components/goal-completed-dialog.component';
import { GoalSse } from '../data/goal.sse';
import { GoalStore } from '../state/goal.store';

@Component({
  selector: 'app-goal-dashboard',
  imports: [GoalCard, CreateGoalForm, GoalCompletedDialog],
  templateUrl: './goal-dashboard.component.html',
  styleUrl: './goal-dashboard.component.css',
})
export class GoalDashboard implements OnInit, OnDestroy {
  private readonly store = inject(GoalStore);
  private readonly sse = inject(GoalSse);

  protected readonly goals = this.store.goals;
  protected readonly loading = this.store.loading;
  protected readonly error = this.store.error;

  ngOnInit(): void {
    this.store.load();
    this.sse.connect();
  }

  ngOnDestroy(): void {
    this.sse.disconnect();
  }
}
