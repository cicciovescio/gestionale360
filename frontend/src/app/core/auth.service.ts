import { Injectable, signal } from '@angular/core';
import { User } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  user = signal<User | null>(this.readUser());

  get token(): string | null {
    return localStorage.getItem('meridiana.token');
  }

  loggedIn(): boolean {
    return !!this.token;
  }

  setSession(token: string, user: User): void {
    localStorage.setItem('meridiana.token', token);
    localStorage.setItem('meridiana.user', JSON.stringify(user));
    this.user.set(user);
  }

  logout(): void {
    localStorage.removeItem('meridiana.token');
    localStorage.removeItem('meridiana.user');
    this.user.set(null);
  }

  private readUser(): User | null {
    const raw = localStorage.getItem('meridiana.user');
    return raw ? JSON.parse(raw) as User : null;
  }
}
