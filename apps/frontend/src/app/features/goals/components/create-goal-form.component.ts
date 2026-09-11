import { Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { GoalStore } from '../state/goal.store';

@Component({
  selector: 'app-create-goal-form',
  imports: [ReactiveFormsModule],
  templateUrl: './create-goal-form.component.html',
  styleUrl: './create-goal-form.component.css',
})
export class CreateGoalForm {
  private readonly store = inject(GoalStore);
  private readonly fb = inject(NonNullableFormBuilder);

  readonly form = this.fb.group({
    name: ['', Validators.required],
    targetAmount: this.fb.control<number | null>(null, {
      validators: [Validators.required, Validators.min(1)],
    }),
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const name = this.form.controls.name.getRawValue().trim();
    const targetAmount = this.form.controls.targetAmount.getRawValue();
    if (!name || targetAmount === null) {
      this.form.controls.name.setErrors({ required: true });
      return;
    }
    this.store.create(name, targetAmount);
    this.form.reset();
  }
}
