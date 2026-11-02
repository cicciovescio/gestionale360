import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { CONVERT_MAP, DOC_LABELS, Doc } from '../core/models';
import { day, euro, statusChip, statusLabel } from '../core/money';

@Component({
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="hero">
      <div>
        <h1>{{ title }}</h1>
        <p>Lista densa, anteprima chiara. Conferma, converti, invia: tre azioni, non tre schermate.</p>
      </div>
      <a class="btn accent" [routerLink]="newPath">Nuovo</a>
    </div>
    <div class="table-card">
      <table class="table">
        <thead>
          <tr>
            <th>Numero</th><th>Controparte</th><th>Data</th><th>Stato</th><th>FE</th><th>Totale</th>
          </tr>
        </thead>
        <tbody>
          @for (d of rows(); track d.id) {
            <tr [routerLink]="['/app/documenti', d.id]" style="cursor:pointer">
              <td>{{ d.number || 'Bozza' }}</td>
              <td><b>{{ d.partyName }}</b></td>
              <td>{{ day(d.date) }}</td>
              <td><span [class]="statusChip(d.status)">{{ statusLabel(d.status) }}</span></td>
              <td>{{ statusLabel(d.einvoiceStatus) }}</td>
              <td>{{ euro(d.total) }}</td>
            </tr>
          }
        </tbody>
      </table>
      @if (!rows().length) {
        <div class="empty">
          <h3>Nessun documento</h3>
          <p>Parti da una bozza: il numero definitivo arriva solo alla conferma.</p>
        </div>
      }
    </div>
  `
})
export class DocumentListComponent {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  title = '';
  type = '';
  newPath = '';
  rows = signal<Doc[]>([]);
  euro = euro; day = day; statusChip = statusChip; statusLabel = statusLabel;
  labels = DOC_LABELS;
  convert = CONVERT_MAP;

  constructor() {
    this.route.data.subscribe(d => {
      this.type = d['type'];
      this.title = d['title'];
      this.newPath = d['newPath'];
      this.api.documents(this.type).subscribe(r => this.rows.set(r));
    });
  }
}
