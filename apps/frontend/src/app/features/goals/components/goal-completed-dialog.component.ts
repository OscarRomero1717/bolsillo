import { Component, inject } from '@angular/core';
import { GoalStore } from '../state/goal.store';

@Component({
  selector: 'app-goal-completed-dialog',
  templateUrl: './goal-completed-dialog.component.html',
  styleUrl: './goal-completed-dialog.component.css',
  host: {
    '(document:keydown.escape)': 'onEscape()',
  },
})
export class GoalCompletedDialog {
  private readonly store = inject(GoalStore);

  protected readonly goal = this.store.completedGoal;

  protected dismiss(): void {
    this.store.dismissCompleted();
  }

  protected onEscape(): void {
    if (this.goal()) {
      this.dismiss();
    }
  }
}
