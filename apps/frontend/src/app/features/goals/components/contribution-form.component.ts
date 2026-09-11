import { Component, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { GoalStore } from '../state/goal.store';

@Component({
  selector: 'app-contribution-form',
  imports: [ReactiveFormsModule],
  templateUrl: './contribution-form.component.html',
  styleUrl: './contribution-form.component.css',
})
export class ContributionForm {
  readonly goalId = input.required<string>();

  private readonly store = inject(GoalStore);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly showStoreError = signal(false);

  readonly form = this.fb.group({
    amount: this.fb.control<number | null>(null, {
      validators: [Validators.required, Validators.min(1)],
    }),
  });

  protected readonly error = this.store.error;

  protected visibleError(): string | null {
    return this.showStoreError() ? this.error() : null;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const amount = this.form.controls.amount.getRawValue();
    if (amount === null) {
      return;
    }
    this.showStoreError.set(true);
    this.store.contribute(this.goalId(), amount);
    this.form.reset();
  }
}
