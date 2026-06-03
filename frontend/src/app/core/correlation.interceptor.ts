import { HttpInterceptorFn } from '@angular/common/http';

export const correlationInterceptor: HttpInterceptorFn = (req, next) => {
  const withCorrelation = req.clone({
    setHeaders: { 'X-Correlation-Id': crypto.randomUUID() },
  });
  return next(withCorrelation);
};
