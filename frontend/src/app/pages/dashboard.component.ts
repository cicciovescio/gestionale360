import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Dashboard } from '../core/models';
import { day, euro, statusChip, statusLabel } from '../core/money';

@Component({
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="hero">
      <div>
        <h1>Buongiorno, Bruno.</h1>
        <p>Oggi hai {{ data()?.overdue || 0 }} {{ (data()?.overdue || 0) === 1 ? 'scadenza in ritardo' : 'scadenze in ritardo' }} e {{ data()?.draftDocuments || 0 }} documenti in bozza. Il tavolo è pronto.</p>
      </div>
      <a class="btn accent" routerLink="/app/vendite/fatture/new">Nuova fattura</a>
    </div>
    <div class="kpis">
      <div class="kpi credit"><span>Crediti clienti</span><strong>{{ euro(data()?.receivables) }}</strong><em>IVA inclusa</em></div>
      <div class="kpi debt"><span>Debiti fornitori</span><strong>{{ euro(data()?.payables) }}</strong><em>IVA inclusa</em></div>
      <div class="kpi"><span>Vendite del mese</span><strong>{{ euro(data()?.monthSales) }}</strong></div>
      <div class="kpi"><span>Acquisti del mese</span><strong>{{ euro(data()?.monthPurchases) }}</strong></div>
    </div>
    <div class="grid-2">
      <div class="panel">
        <h2>Fatturato 2026</h2>
        <div class="bars">
          @for (m of data()?.monthly || []; track m.month) {
            <div class="bar">
              <i [style.height.%]="bar(m.sales)"></i>
              <b>{{ m.month }}</b>
            </div>
          }
        </div>
      </div>
      <div class="panel">
        <h2>Prossime scadenze</h2>
        @for (i of data()?.upcoming || []; track i.id) {
          <div style="display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid var(--line)">
            <div>
              <b>{{ i.partyName }}</b>
              <div class="muted">{{ i.documentNumber }} · {{ day(i.dueDate) }}</div>
            </div>
            <b>{{ euro(i.residual) }}</b>
          </div>
        }
      </div>
    </div>
    <div class="table-card">
      <table class="table">
        <thead><tr><th>Documento</th><th>Cliente</th><th>Data</th><th>Stato</th><th>Totale</th></tr></thead>
        <tbody>
          @for (d of data()?.recentSales || []; track d.id) {
            <tr [routerLink]="['/app/documenti', d.id]" style="cursor:pointer">
              <td>{{ d.number || 'Bozza' }}</td>
              <td>{{ d.partyName }}</td>
              <td>{{ day(d.date) }}</td>
              <td><span [class]="statusChip(d.status)">{{ statusLabel(d.status) }}</span></td>
              <td>{{ euro(d.total) }}</td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `
})
export class DashboardComponent {
  private api = inject(ApiService);
  data = signal<Dashboard | null>(null);
  euro = euro; day = day; statusChip = statusChip; statusLabel = statusLabel;
  constructor() { this.api.dashboard().subscribe(d => this.data.set(d)); }
  bar(v: number) {
    const max = Math.max(...(this.data()?.monthly.map(m => m.sales) || [1]), 1);
    return Math.max(8, (Number(v) / max) * 100);
  }
}
