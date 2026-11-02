import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Installment } from '../core/models';
import { day, euro } from '../core/money';

@Component({
  standalone: true,
  template: `
    <div class="hero">
      <div>
        <h1>{{ receivable ? 'Scadenze clienti' : 'Scadenze fornitori' }}</h1>
        <p>Incassa o paga dalla riga. Il residuo e la prima nota si aggiornano da soli.</p>
      </div>
    </div>
    <div class="table-card">
      <table class="table">
        <thead><tr><th>Scadenza</th><th>Controparte</th><th>Documento</th><th>Importo</th><th>Residuo</th><th></th></tr></thead>
        <tbody>
          @for (i of rows(); track i.id) {
            <tr>
              <td>{{ day(i.dueDate) }}</td>
              <td><b>{{ i.partyName }}</b></td>
              <td>{{ i.documentNumber }}</td>
              <td>{{ euro(i.amount) }}</td>
              <td>{{ euro(i.residual) }}</td>
              <td class="row-actions">
                @if (i.residual > 0) {
                  <button class="btn small accent" (click)="pay(i)">{{ receivable ? 'Incassa' : 'Paga' }}</button>
                  <button class="btn small ghost" (click)="remind(i)" [disabled]="i.reminderSent">Sollecito</button>
                } @else { <span class="chip ok">chiusa</span> }
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `
})
export class TreasuryComponent {
  private api = inject(ApiService);
  receivable = true;
  rows = signal<Installment[]>([]);
  euro = euro; day = day;
  constructor(route: ActivatedRoute) {
    route.data.subscribe(d => {
      this.receivable = d['receivable'];
      this.reload();
    });
  }
  reload() { this.api.installments(this.receivable).subscribe(r => this.rows.set(r)); }
  pay(i: Installment) { this.api.pay(i.id, i.residual, 'BONIFICO').subscribe(() => this.reload()); }
  remind(i: Installment) { this.api.remind(i.id).subscribe(() => this.reload()); }
}
