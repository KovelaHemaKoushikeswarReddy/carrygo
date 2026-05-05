import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NgIf } from '@angular/common';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink, NgIf],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class Register {
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

  showPassword        = false;
  showConfirmPassword = false;

  constructor(private authService: AuthService) {}

  get passwordValid(): boolean {
    const p = this.user.password;
    return p.length >= 8 && /[A-Z]/.test(p) && /[0-9]/.test(p);
  }

  get passwordsMatch(): boolean {
    return this.user.password === this.user.confirmPassword;
  }

  // ── Submit ────────────────────────────────────────────────────────────────
  onSubmit() {
    if (!this.passwordValid || !this.passwordsMatch) return;

    this.authService.register(this.user).subscribe({
      next: () => alert('Registration successful'),
      error: () => alert('Registration failed')
    });
  }
}
