import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="auth-shell">
      <section class="auth-panel">
        <div class="auth-card">
          <img class="auth-logo" src="/gestiamo360-logo.png" alt="Gestiamo 360">
          @if (error()) { <div class="login-error">{{ error() }}</div> }
          <form (ngSubmit)="submit()" class="form-grid" style="grid-template-columns:1fr">
            <div class="field span-2">
              <label>Email</label>
              <input name="email" [(ngModel)]="email" type="email" placeholder="Email" required>
            </div>
            <div class="field span-2">
              <label>Password</label>
              <input name="password" [(ngModel)]="password" type="password" placeholder="Password" required>
            </div>
            <button class="btn accent" type="submit" [disabled]="loading()">{{ loading() ? 'Accesso…' : 'Login' }}</button>
          </form>
        </div>
      </section>
    </div>
  `
})
export class LoginComponent {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);
  email = 'bruno@mposta.it';
  password = 'Bruno2006@';
  loading = signal(false);
  error = signal('');

  submit() {
    this.loading.set(true);
    this.error.set('');
    this.api.login(this.email, this.password).subscribe({
      next: res => {
        this.auth.setSession(res.token, res.user);
        this.router.navigateByUrl('/app/dashboard');
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Email o password non corrette.');
      }
    });
  }
}
