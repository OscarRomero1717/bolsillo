import { Component, effect, inject, input, signal } from '@angular/core';
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
  readonly remaining = input.required<number>();

  private readonly store = inject(GoalStore);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly showStoreError = signal(false);

  readonly form = this.fb.group({
    amount: this.fb.control<number | null>(null, {
      validators: [Validators.required, Validators.min(0.01)],
    }),
  });

  protected readonly error = this.store.error;
  protected readonly loading = this.store.loading;

  constructor() {
    this.form.controls.amount.valueChanges.subscribe((value) => {
      if (value !== null) {
        this.showStoreError.set(false);
      }
    });
    effect(() => {
      const remaining = this.remaining();
      this.form.controls.amount.setValidators([
        Validators.required,
        Validators.min(0.01),
        Validators.max(remaining),
      ]);
      this.form.controls.amount.updateValueAndValidity({ emitEvent: false });
    });
  }

  protected visibleError(): string | null {
    return this.showStoreError() ? this.error() : null;
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.controls.amount.getRawValue();
    if (raw === null) {
      return;
    }
    const amount = Math.round(raw * 100) / 100;
    this.showStoreError.set(true);
    this.store.contribute(this.goalId(), amount);
    this.form.reset();
  }
}
