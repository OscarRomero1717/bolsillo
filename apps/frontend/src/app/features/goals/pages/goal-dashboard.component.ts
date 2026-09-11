import { Component, OnInit, inject } from '@angular/core';
import { GoalCard } from '../components/goal-card.component';
import { CreateGoalForm } from '../components/create-goal-form.component';
import { GoalCompletedDialog } from '../components/goal-completed-dialog.component';
import { GoalStore } from '../state/goal.store';

@Component({
  selector: 'app-goal-dashboard',
  imports: [GoalCard, CreateGoalForm, GoalCompletedDialog],
  templateUrl: './goal-dashboard.component.html',
  styleUrl: './goal-dashboard.component.css',
})
export class GoalDashboard implements OnInit {
  private readonly store = inject(GoalStore);

  protected readonly goals = this.store.goals;
  protected readonly loading = this.store.loading;
  protected readonly error = this.store.error;

  ngOnInit(): void {
    this.store.load();
  }
}
