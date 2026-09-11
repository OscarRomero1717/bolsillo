import { DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { ContributionForm } from './contribution-form.component';
import { Goal } from '../models/goal.model';

@Component({
  selector: 'app-goal-card',
  imports: [DecimalPipe, ContributionForm],
  templateUrl: './goal-card.component.html',
  styleUrl: './goal-card.component.css',
})
export class GoalCard {
  readonly goal = input.required<Goal>();

  protected remaining(): number {
    const goal = this.goal();
    return roundCents(goal.targetAmount - goal.currentAmount);
  }
}

function roundCents(value: number): number {
  return Math.round(value * 100) / 100;
}
