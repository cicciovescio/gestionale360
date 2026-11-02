import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Doc } from '../core/models';
import { day, euro, statusLabel } from '../core/money';

@Component({
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="hero">
      <div>
        <h1>Fatture elettroniche</h1>
        <p>Invio, esito e XML nello stesso posto. In demo l’invio SDI viene simulato e accettato.</p>
      </div>
    </div>
    <div class="table-card">
      <table class="table">
        <thead><tr><th>Numero</th><th>Cliente</th><th>Data</th><th>Esito</th><th>Totale</th><th></th></tr></thead>
        <tbody>
          @for (d of rows(); track d.id) {
            <tr>
              <td>{{ d.number }}</td>
              <td>{{ d.partyName }}</td>
              <td>{{ day(d.date) }}</td>
              <td>{{ statusLabel(d.einvoiceStatus) }}</td>
              <td>{{ euro(d.total) }}</td>
              <td>
                @if (d.status === 'CONFIRMED') {
                  <button class="btn small accent" (click)="send(d)">Invia</button>
                } @else {
                  <a class="btn small ghost" [routerLink]="['/app/documenti', d.id]">Apri</a>
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `
})
export class XmlComponent {
  api = inject(ApiService);
  rows = signal<Doc[]>([]);
  euro = euro; day = day; statusLabel = statusLabel;
  constructor() { this.reload(); }
  reload() { this.api.documents('SALE_INVOICE').subscribe(r => this.rows.set(r)); }
  send(d: Doc) { if (d.id) this.api.einvoice(d.id).subscribe(() => this.reload()); }
}
