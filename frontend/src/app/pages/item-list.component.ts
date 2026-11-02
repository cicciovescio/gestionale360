import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Item } from '../core/models';
import { euro } from '../core/money';

@Component({
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="hero">
      <div>
        <h1>Prodotti e servizi</h1>
        <p>Catalogo unico: il prezzo, l’IVA e la giacenza stanno sulla stessa riga.</p>
      </div>
      <a class="btn accent" routerLink="/app/catalogo/articoli/new">Nuovo articolo</a>
    </div>
    <div class="table-card">
      <table class="table">
        <thead><tr><th>Codice</th><th>Descrizione</th><th>Categoria</th><th>Prezzo</th><th>Giacenza</th></tr></thead>
        <tbody>
          @for (i of rows(); track i.id) {
            <tr [routerLink]="['/app/catalogo/articoli', i.id]" style="cursor:pointer">
              <td>{{ i.code }}</td>
              <td><b>{{ i.name }}</b></td>
              <td>{{ i.category }}</td>
              <td>{{ euro(i.listPrice) }}</td>
              <td>{{ i.stockManaged ? i.stockQty : 'servizio' }}</td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `
})
export class ItemListComponent {
  private api = inject(ApiService);
  rows = signal<Item[]>([]);
  euro = euro;
  constructor() { this.api.items().subscribe(r => this.rows.set(r)); }
}
