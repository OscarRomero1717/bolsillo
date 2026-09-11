export type GoalStatus = 'OPEN' | 'COMPLETED';

export interface Goal {
  id: string;
  name: string;
  targetAmount: number;
  currentAmount: number;
  progressPercent: number;
  status: GoalStatus;
  version: number;
}

export interface CreateGoalRequest {
  name: string;
  targetAmount: number;
}

export interface ContributionRequest {
  amount: number;
}
