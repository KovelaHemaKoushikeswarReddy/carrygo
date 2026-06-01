import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from './auth.service';

// Route guard — blocks navigation to protected pages unless the user is logged in.
// Wired up in app.routes.ts via canActivate: [AuthGuard].
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (this.authService.isLoggedIn()) return true;

    // Not logged in — bounce them to the login screen.
    this.router.navigate(['/login']);
    return false;
  }
}
