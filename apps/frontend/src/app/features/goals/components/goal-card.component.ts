import { DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { Goal } from '../models/goal.model';

@Component({
  selector: 'app-goal-card',
  imports: [DecimalPipe],
  templateUrl: './goal-card.component.html',
  styleUrl: './goal-card.component.css',
})
export class GoalCard {
  readonly goal = input.required<Goal>();
}
