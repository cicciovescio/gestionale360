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
      <section class="auth-visual">
        <img class="auth-logo auth-logo-dark" src="/gestiamo360-logo.png" alt="Gestiamo 360">
        <div class="auth-quote">
          Il gestionale che non ti fa perdere il filo.
          <small>Fatture, magazzino, scadenze e SDI in un unico tavolo di lavoro, senza il rumore delle vecchie griglie.</small>
        </div>
        <div>Bruno Servizi · ambiente locale</div>
      </section>
      <section class="auth-panel">
        <div class="auth-card">
          <img class="auth-logo" src="/gestiamo360-logo.png" alt="Gestiamo 360">
          <h1>Bentornato.</h1>
          <p>Entra per riprendere documenti, giacenze e incassi.</p>
          @if (error()) { <div class="login-error">{{ error() }}</div> }
          <form (ngSubmit)="submit()" class="form-grid" style="grid-template-columns:1fr">
            <div class="field span-2">
              <label>Email</label>
              <input name="email" [(ngModel)]="email" type="email" required>
            </div>
            <div class="field span-2">
              <label>Password</label>
              <input name="password" [(ngModel)]="password" type="password" required>
            </div>
            <button class="btn accent" type="submit" [disabled]="loading()">{{ loading() ? 'Accesso…' : 'Entra in Meridiana' }}</button>
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
