import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Register } from './register/register';
import { UserDashboard } from './user-dashboard/user-dashboard';
import { PorterDashboardComponent } from './homepage-porter/porter-homepage';
import { PorterDeliveriesComponent } from './porter-deliveries/porter-deliveries';
import { PorterKycComponent } from './porter-kyc/porter-kyc';
import { PorterProfileComponent } from './porter-profile/porter-profile';
import { AuthGuard } from './services/auth.guard';
import { PorterGuard } from './services/porter.guard';
import { SendParcelComponent } from './send-parcel/send-parcel';

// Maps URL paths to components. The router renders these inside <router-outlet>.
// AuthGuard blocks pages until the user logs in; PorterGuard further requires the porter role.
export const routes: Routes = [
  // Default route — send the user to login.
  { path: '', redirectTo: '/login', pathMatch: 'full' },

  // Public pages.
  { path: 'login', component: Login },
  { path: 'register', component: Register },

  // User-only pages (must be logged in).
  { path: 'user-dashboard/:userId', component: UserDashboard, canActivate: [AuthGuard] },
  { path: 'user-dashboard', component: UserDashboard, canActivate: [AuthGuard] },
  { path: 'send-parcel/:userId', component: SendParcelComponent, canActivate: [AuthGuard] },
  { path: 'send-parcel', component: SendParcelComponent, canActivate: [AuthGuard] },


  // Porter-only pages (must be logged in AND have the porter role).
  { path: 'porter-dashboard/:userId', component: PorterDashboardComponent, canActivate: [PorterGuard] },
  { path: 'porter-dashboard', component: PorterDashboardComponent, canActivate: [PorterGuard] },
  { path: 'porter-deliveries/:userId', component: PorterDeliveriesComponent, canActivate: [PorterGuard] },
  { path: 'porter-deliveries', component: PorterDeliveriesComponent, canActivate: [PorterGuard] },
  { path: 'porter-kyc/:userId', component: PorterKycComponent, canActivate: [PorterGuard] },
  { path: 'porter-kyc', component: PorterKycComponent, canActivate: [PorterGuard] },
  { path: 'porter-profile/:userId', component: PorterProfileComponent, canActivate: [PorterGuard] },
  { path: 'porter-profile', component: PorterProfileComponent, canActivate: [PorterGuard] },

  // Any unknown URL falls back to login.
  { path: '**', redirectTo: '/login' }
];
