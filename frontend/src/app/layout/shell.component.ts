import { Component, HostListener, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { NAV, NavItem } from './nav';

@Component({
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-shell">
      <aside class="sidebar">
        <div class="brand">
          <img class="brand-logo" src="/gestiamo360-logo.png" alt="Gestiamo 360">
        </div>
        <div class="side-search">
          <input placeholder="Cerca o premi Ctrl K" (focus)="palette.set(true)">
        </div>
        <nav class="nav">
          @for (group of nav; track group.label) {
            <div class="nav-block">
              <button class="nav-group" type="button" (click)="toggle(group.label)" [class.open]="isOpen(group.label)">
                <span>{{ group.label }}</span>
                <i class="chevron"></i>
              </button>
              @if (isOpen(group.label)) {
                <div class="nav-items">
                  @for (item of group.items; track item.path) {
                    <a [routerLink]="item.path" routerLinkActive="active">{{ item.label }}</a>
                  }
                </div>
              }
            </div>
          }
        </nav>
        <div class="userbox">
          <div class="avatar">{{ initials() }}</div>
          <div>
            <div>{{ auth.user()?.fullName }}</div>
            <button class="nav-link" (click)="logout()">Esci</button>
          </div>
        </div>
      </aside>
      <main class="workspace">
        <header class="topbar">
          <div class="crumbs">Meridiana · <b>{{ title }}</b></div>
          <div class="filters">
            <span class="chip ok">FE {{ credits }} crediti</span>
            <button class="btn ghost small" (click)="palette.set(true)">Cerca</button>
          </div>
        </header>
        <section class="page">
          <router-outlet />
        </section>
      </main>
    </div>
    @if (palette()) {
      <div class="palette" (click)="palette.set(false)">
        <div class="palette-box" (click)="$event.stopPropagation()">
          <input #q placeholder="Vai a clienti, fatture, magazzino…" (input)="filter(q.value)" autofocus>
          <ul>
            @for (item of filtered(); track item.path) {
              <li (click)="go(item.path)">
                <span>{{ item.label }}</span>
                <span class="muted">{{ item.group }}</span>
              </li>
            }
          </ul>
        </div>
      </div>
    }
  `
})
export class ShellComponent {
  auth = inject(AuthService);
  private router = inject(Router);
  nav = NAV;
  title = 'Home';
  credits = 1486;
  palette = signal(false);
  filtered = signal<NavItem[]>([]);
  openGroups = signal<Record<string, boolean>>({});
  all: NavItem[] = NAV.flatMap(g => g.items.map(i => ({ ...i, group: g.label })));

  constructor() {
    this.filtered.set(this.all);
    this.syncFromUrl(this.router.url);
    this.router.events.pipe(filter(e => e instanceof NavigationEnd)).subscribe(e => {
      this.syncFromUrl((e as NavigationEnd).urlAfterRedirects);
    });
  }

  isOpen(label: string): boolean {
    return !!this.openGroups()[label];
  }

  toggle(label: string): void {
    this.openGroups.update(state => ({ ...state, [label]: !state[label] }));
  }

  private syncFromUrl(url: string): void {
    const found = this.all.find(i => url.startsWith(i.path));
    this.title = found?.label || 'Meridiana';
    if (found?.group) {
      this.openGroups.update(state => ({ ...state, [found.group!]: true }));
    }
  }

  initials() {
    return (this.auth.user()?.fullName || 'B').split(' ').map(p => p[0]).join('').slice(0, 2);
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }

  filter(q: string) {
    const s = q.toLowerCase();
    this.filtered.set(this.all.filter(i =>
      i.label.toLowerCase().includes(s) || (i.group || '').toLowerCase().includes(s)
    ));
  }

  go(path: string) {
    this.palette.set(false);
    this.router.navigateByUrl(path);
  }

  @HostListener('document:keydown', ['$event'])
  keys(e: KeyboardEvent) {
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
      e.preventDefault();
      this.palette.set(true);
    }
    if (e.key === 'Escape') this.palette.set(false);
  }
}
