import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { Work } from '../core/models';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="hero">
      <div>
        <h1>Planning e interventi</h1>
        <p>Tecnici, commesse e ferie sullo stesso calendario. Domani si vede da qui.</p>
      </div>
    </div>
    <div class="grid-2">
      <div class="panel">
        <h3>Nuovo evento</h3>
        <div class="form-grid">
          <div class="field"><label>Tipo</label>
            <select [(ngModel)]="draft.kind" name="kind">
              <option>PLANNING</option><option>INTERVENTION</option><option>JOB</option><option>HOLIDAY</option>
            </select>
          </div>
          <div class="field"><label>Stato</label><input [(ngModel)]="draft.status" name="status"></div>
          <div class="field span-2"><label>Titolo</label><input [(ngModel)]="draft.title" name="title"></div>
          <div class="field"><label>Cliente</label><input [(ngModel)]="draft.customerName" name="c"></div>
          <div class="field"><label>Tecnico</label><input [(ngModel)]="draft.technicianName" name="t"></div>
          <div class="field span-2"><label>Inizio</label><input type="datetime-local" [(ngModel)]="draft.startAt" name="start"></div>
        </div>
        <button class="btn accent" style="margin-top:12px" (click)="save()">Salva</button>
      </div>
      <div class="panel">
        @for (e of rows(); track e.id) {
          <div style="padding:12px 0;border-bottom:1px solid var(--line)">
            <div class="chip info">{{ e.kind }}</div>
            <h3 style="margin-top:8px">{{ e.title }}</h3>
            <div class="muted">{{ e.customerName }} · {{ e.technicianName }} · {{ e.status }}</div>
          </div>
        }
      </div>
    </div>
  `
})
export class EventsComponent {
  api = inject(ApiService);
  rows = signal<Work[]>([]);
  draft: Work = { kind: 'INTERVENTION', title: '', status: 'Programmato' };
  constructor() { this.reload(); }
  reload() { this.api.events().subscribe(r => this.rows.set(r)); }
  save() { this.api.saveEvent(this.draft).subscribe(() => this.reload()); }
}
