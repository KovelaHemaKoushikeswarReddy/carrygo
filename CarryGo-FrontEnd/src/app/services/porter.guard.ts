import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from './auth.service';

// Route guard for porter-only pages.
// Requires the user to be logged in AND have "porter" in their roles list.
// Non-porters are bounced to the regular user dashboard instead of /login.
@Injectable({ providedIn: 'root' })
export class PorterGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    // Not signed in at all → login.
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return false;
    }

    // The role column can be a comma-separated list like "user,porter".
    const role = (this.authService.getUserRole() ?? '').toLowerCase();
    const roles = role.split(',').map(r => r.trim());
    const isPorter = roles.includes('porter') || roles.includes('commuter');

    if (isPorter) return true;

    // Signed in but not a porter → send them to the regular user dashboard.
    const userId = this.authService.getLoggedInUserId();
    this.router.navigate(userId ? ['/user-dashboard', userId] : ['/user-dashboard']);
    return false;
  }
}
