import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

/** On another device not paired yet, the server answers nothing but pairing: the page to pair it opens. */
export const deviceGuardInterceptor: HttpInterceptorFn = (request, next) => {
  const router = inject(Router);
  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && !router.url.startsWith('/pair')) {
        void router.navigate(['/pair']);
      }
      return throwError(() => error);
    }),
  );
};
