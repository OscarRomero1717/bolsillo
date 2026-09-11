import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ContributionRequest, CreateGoalRequest, Goal } from '../models/goal.model';

@Injectable({ providedIn: 'root' })
export class GoalApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/goals`;

  list(): Observable<Goal[]> {
    return this.http.get<Goal[]>(this.baseUrl);
  }

  create(request: CreateGoalRequest): Observable<Goal> {
    return this.http.post<Goal>(this.baseUrl, request);
  }

  contribute(id: string, request: ContributionRequest): Observable<Goal> {
    return this.http.post<Goal>(`${this.baseUrl}/${id}/contributions`, request);
  }
}
