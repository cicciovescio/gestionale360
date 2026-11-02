import { Component, inject, signal } from '@angular/core';
import { ApiService } from '../core/api.service';
import { Item, Stock } from '../core/models';
import { day } from '../core/money';

@Component({
  standalone: true,
  template: `
    <div class="hero">
      <div>
        <h1>{{ mode === 'qty' ? 'Disponibilità' : 'Movimenti magazzino' }}</h1>
        <p>Ogni conferma di DDT o fattura lascia una traccia. Niente giacenze fantasma.</p>
      </div>
    </div>
    @if (mode === 'qty') {
      <div class="table-card">
        <table class="table">
          <thead><tr><th>Codice</th><th>Articolo</th><th>Deposito</th><th>Giacenza</th></tr></thead>
          <tbody>
            @for (i of items(); track i.id) {
              <tr>
                <td>{{ i.code }}</td><td>{{ i.name }}</td><td>{{ i.warehouseName }}</td>
                <td><b>{{ i.stockManaged ? i.stockQty : '—' }}</b></td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    } @else {
      <div class="table-card">
        <table class="table">
          <thead><tr><th>Data</th><th>Deposito</th><th>Articolo</th><th>Qta</th><th>Documento</th></tr></thead>
          <tbody>
            @for (s of moves(); track s.id) {
              <tr>
                <td>{{ day(s.date) }}</td><td>{{ s.warehouseName }}</td><td>{{ s.itemName }}</td>
                <td [style.color]="s.qty < 0 ? 'var(--bad)' : 'var(--ok)'">{{ s.qty }}</td>
                <td>{{ s.documentNumber }} · {{ s.reason }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `
})
export class StockComponent {
  api = inject(ApiService);
  mode: 'qty' | 'moves' = 'moves';
  items = signal<Item[]>([]);
  moves = signal<Stock[]>([]);
  day = day;
  constructor() {
    const url = location.pathname;
    this.mode = url.includes('disponibilita') ? 'qty' : 'moves';
    if (this.mode === 'qty') this.api.items().subscribe(r => this.items.set(r));
    else this.api.stock().subscribe(r => this.moves.set(r));
  }
}
