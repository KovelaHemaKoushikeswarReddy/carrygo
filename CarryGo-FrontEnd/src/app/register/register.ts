import { Component, ViewChild } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NgIf } from '@angular/common';
import { AuthService } from '../services/auth.service';

// Registration screen — validates form input client-side and calls AuthService.register.
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink, NgIf],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class Register {
  // Bound to the form fields in register.html.
  user = {
    name: '',
    email: '',
    phone: '',
    countryCode: '+91',
    password: '',
    confirmPassword: '',
    role: 'user'
  };

  get isCommuter(): boolean { return this.user.role === 'commuter'; }

  // Regex used by the template to validate the phone field based on selected country.
  get phonePattern(): string {
    switch (this.user.countryCode) {
      case '+91': return '^[6-9][0-9]{9}$';
      case '+1':  return '^[2-9][0-9]{9}$';
      case '+44': return '^7[0-9]{9}$';
      default:    return '^[0-9]{10}$';
    }
  }

  // User-friendly message shown when the phone field doesn't match the pattern.
  get phoneError(): string {
    switch (this.user.countryCode) {
      case '+91': return 'India numbers must be 10 digits and start with 6-9';
      case '+1':  return 'USA numbers must be 10 digits and start with 2-9';
      case '+44': return 'UK mobile numbers must be 10 digits and start with 7';
      default:    return 'Phone number must be 10 digits';
    }
  }

  showPassword = false;
  showConfirmPassword = false;

  @ViewChild('registerForm') registerForm?: NgForm;

  constructor(private authService: AuthService) {}

  // Triggered on form submit. Checks password strength + match, then calls the backend.
  onSubmit() {
    // Password must have ≥8 chars, 1 uppercase, 1 digit, and 1 special character.
    const passwordPattern = /^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()\-_+=\[\]{};:',.<>/?]).{8,}$/;
    if (!passwordPattern.test(this.user.password)) {
      alert('Password must be at least 8 characters and include 1 uppercase letter, 1 number, and 1 special character.');
      return;
    }

    if (this.user.password !== this.user.confirmPassword) {
      alert('Passwords do not match!');
      return;
    }

    this.authService.register(this.user).subscribe({
      next: () => alert('Registration successful'),
      error: () => alert('Registration failed')
    });
  }

  // Clears the form (used by the "Reset" button on the page).
  resetForm() {
    this.user = {
      name: '',
      email: '',
      phone: '',
      countryCode: '+91',
      password: '',
      confirmPassword: '',
      role: this.user.role
    };
    this.showPassword = false;
    this.showConfirmPassword = false;
    this.registerForm?.resetForm({
      name: '',
      email: '',
      phone: '',
      countryCode: '+91',
      password: '',
      confirmPassword: ''
    });
  }
}
