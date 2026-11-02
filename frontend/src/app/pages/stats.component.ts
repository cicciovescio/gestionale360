import { Component, inject, signal } from '@angular/core';
import { ApiService } from '../core/api.service';
import { euro } from '../core/money';

@Component({
  standalone: true,
  template: `
    <div class="hero"><div><h1>Statistiche</h1><p>Fatturato, clienti e IVA senza esportare niente in Excel.</p></div></div>
    <div class="panel">
      <h2>Andamento mensile</h2>
      <div class="bars">
        @for (m of data()?.monthly || []; track m.month) {
          <div class="bar"><i [style.height.%]="bar(m.sales)"></i><b>{{ m.month }}</b></div>
        }
      </div>
    </div>
    <div class="grid-2">
      <div class="panel">
        <h3>Per cliente</h3>
        @for (r of data()?.byCustomer || []; track r.label) {
          <div style="display:flex;justify-content:space-between;padding:8px 0"><span>{{ r.label }}</span><b>{{ euro(r.total) }}</b></div>
        }
      </div>
      <div class="panel">
        <h3>Per prodotto</h3>
        @for (r of data()?.byItem || []; track r.label) {
          <div style="display:flex;justify-content:space-between;padding:8px 0"><span>{{ r.label }}</span><b>{{ euro(r.total) }}</b></div>
        }
      </div>
    </div>
  `
})
export class StatsComponent {
  api = inject(ApiService);
  data = signal<any>(null);
  euro = euro;
  constructor() { this.api.stats().subscribe(d => this.data.set(d)); }
  bar(v: number) {
    const max = Math.max(...(this.data()?.monthly?.map((m: any) => m.sales) || [1]), 1);
    return Math.max(8, (Number(v) / max) * 100);
  }
}
