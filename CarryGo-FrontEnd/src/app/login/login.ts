import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { timeout, TimeoutError } from 'rxjs';

// Login screen — collects email/password/role, calls AuthService.login,
// then routes the user to the dashboard that matches their role.
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, TitleCasePipe, RouterLink, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  // Bound to the form fields in login.html.
  loginData = {
    email: '',
    password: '',
    role: 'user'
  };

  isLoading = false;       // disables the submit button while the request is in flight
  showPassword = false;    // toggles the eye icon to show/hide the password

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  // Triggered when the login form is submitted.
  onSubmit() {
    this.isLoading = true;

    // Give the request 8 seconds before treating it as a timeout.
    this.authService.login(this.loginData).pipe(timeout(8000)).subscribe({
      next: (response) => {
        this.isLoading = false;

        // Send the user to the right dashboard based on their role.
        if (response && response.userId) {
          if (response.role === 'porter' || response.role === 'commuter') {
            this.router.navigate(['/porter-dashboard', response.userId]);
          } else {
            this.router.navigate(['/user-dashboard', response.userId]);
          }
        } else {
          alert('Login successful but user data not found.');
        }
      },
      error: (err) => {
        this.isLoading = false;
        // Show a friendly message depending on what went wrong.
        if (err instanceof TimeoutError) {
          alert('Request timed out. Please check your connection and try again.');
        } else if (err.status === 401 || err.status === 403) {
          alert('Incorrect email or password. Please try again.');
        } else if (err.status === 0) {
          alert('Cannot connect to server. Please try again later.');
        } else {
          const msg = typeof err.error === 'string' ? err.error : err.error?.message;
          alert(msg || 'Login failed. Please try again.');
        }
      }
    });
  }

  // Flips between the "I'm a user" and "I'm a porter" tabs on the form.
  switchRole() {
    this.loginData.role = this.loginData.role === 'user' ? 'porter' : 'user';
  }
}
