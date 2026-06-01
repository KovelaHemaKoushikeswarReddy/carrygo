import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, BehaviorSubject } from 'rxjs';
import { Router } from '@angular/router';

// Handles login, registration, logout, and remembering the current user.
// The logged-in user is stored in sessionStorage so a page refresh keeps them signed in,
// and also exposed as an observable (currentUser$) so other components can react to changes.
@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = 'http://localhost:8081/api/users';

  // Holds the currently logged-in user; null when no one is logged in.
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    // On app start, restore the user from sessionStorage if there was one.
    const stored = sessionStorage.getItem('currentUser');
    if (stored) this.currentUserSubject.next(JSON.parse(stored));
  }

  // POST to backend to create a new account.
  register(user: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, user);
  }

  // POST credentials to the backend; on success, remember the user for this browser session.
  login(credentials: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, credentials).pipe(
      tap((user: any) => {
        if (user && user.userId) {
          sessionStorage.setItem('currentUser', JSON.stringify(user));
          sessionStorage.setItem('userEmail', user.email);
          sessionStorage.setItem('userId', user.userId.toString());
          sessionStorage.setItem('userRole', user.role);
          this.currentUserSubject.next(user);
        }
      })
    );
  }

  // Returns the latest user object held in memory.
  getCurrentUser() {
    return this.currentUserSubject.value;
  }

  getLoggedInUserEmail(): string | null {
    return sessionStorage.getItem('userEmail');
  }

  getLoggedInUserId(): number | null {
    const id = sessionStorage.getItem('userId');
    return id ? parseInt(id, 10) : null;
  }

  getUserRole(): string | null {
    return sessionStorage.getItem('userRole');
  }

  isLoggedIn(): boolean {
    return !!sessionStorage.getItem('currentUser');
  }

  // Wipes session data and sends the user back to /login.
  logout() {
    sessionStorage.removeItem('currentUser');
    sessionStorage.removeItem('userEmail');
    sessionStorage.removeItem('userId');
    sessionStorage.removeItem('userRole');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }
}
