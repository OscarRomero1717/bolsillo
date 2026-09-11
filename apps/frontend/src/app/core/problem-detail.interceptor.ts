import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ProblemDetail } from '../features/goals/models/problem-detail.model';

export class ApiError {
  constructor(
    readonly status: number,
    readonly detail: string,
    readonly code: string | undefined,
    readonly cause: HttpErrorResponse,
  ) {}
}

export const problemDetailInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse)) {
        return throwError(() => error);
      }
      if (error.status === 400 || error.status === 422) {
        const problem = readProblem(error);
        return throwError(
          () =>
            new ApiError(
                error.status,
                problem.detail ?? 'Request is not valid',
                problem.code,
                error,
            ),
        );
      }
      return throwError(() => error);
    }),
  );
};

function readProblem(error: HttpErrorResponse): ProblemDetail {
  const body: unknown = error.error;
  if (typeof body !== 'object' || body === null) {
    return { status: error.status };
  }
  const fields = body as Record<string, unknown>;
  return {
    status: typeof fields['status'] === 'number' ? fields['status'] : error.status,
    title: typeof fields['title'] === 'string' ? fields['title'] : undefined,
    detail: typeof fields['detail'] === 'string' ? fields['detail'] : undefined,
    code: typeof fields['code'] === 'string' ? fields['code'] : undefined,
  };
}
