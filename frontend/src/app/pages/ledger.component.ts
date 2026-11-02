import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { Ledger } from '../core/models';
import { day, euro } from '../core/money';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="hero">
      <div><h1>Prima nota</h1><p>Banca, cassa, clienti e fornitori. Le scritture dei documenti arrivano già pronte.</p></div>
    </div>
    <div class="panel">
      <div class="form-grid">
        <div class="field"><label>Data</label><input type="date" [(ngModel)]="draft.date" name="date"></div>
        <div class="field"><label>Conto</label><input [(ngModel)]="draft.account" name="account"></div>
        <div class="field"><label>Entrata</label><input type="number" [(ngModel)]="draft.income" name="in"></div>
        <div class="field"><label>Uscita</label><input type="number" [(ngModel)]="draft.expense" name="out"></div>
        <div class="field span-2"><label>Descrizione</label><input [(ngModel)]="draft.description" name="desc"></div>
      </div>
      <button class="btn accent" style="margin-top:12px" (click)="add()">Registra</button>
    </div>
    <div class="table-card">
      <table class="table">
        <thead><tr><th>Data</th><th>Conto</th><th>Descrizione</th><th>Entrate</th><th>Uscite</th></tr></thead>
        <tbody>
          @for (m of rows(); track m.id) {
            <tr>
              <td>{{ day(m.date) }}</td><td>{{ m.account }}</td><td>{{ m.description }}</td>
              <td>{{ euro(m.income) }}</td><td>{{ euro(m.expense) }}</td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `
})
export class LedgerComponent {
  api = inject(ApiService);
  rows = signal<Ledger[]>([]);
  draft: Ledger = { date: new Date().toISOString().slice(0,10), account: 'Cassa', income: 0, expense: 0, description: '' };
  euro = euro; day = day;
  constructor() { this.reload(); }
  reload() { this.api.ledger().subscribe(r => this.rows.set(r)); }
  add() { this.api.addLedger(this.draft).subscribe(() => this.reload()); }
}
