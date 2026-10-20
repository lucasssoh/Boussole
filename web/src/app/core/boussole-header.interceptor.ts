import { HttpInterceptorFn } from '@angular/common/http';

/** The server refuses a change that does not carry this header, so that no other web page can make one. */
export const boussoleHeaderInterceptor: HttpInterceptorFn = (request, next) =>
  request.method === 'GET' ? next(request) : next(request.clone({ setHeaders: { 'X-Boussole': '1' } }));
