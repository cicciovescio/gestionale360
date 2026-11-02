import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Party } from '../core/models';

@Component({
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="hero">
      <div>
        <h1>{{ title }}</h1>
        <p>Anagrafiche pulite, con partita IVA, SDI e condizioni di pagamento già pronte per i documenti.</p>
      </div>
      <a class="btn accent" [routerLink]="newPath">Nuovo</a>
    </div>
    <div class="table-card">
      <table class="table">
        <thead><tr><th>Nome</th><th>Città</th><th>P.IVA</th><th>Email</th><th>Pagamento</th></tr></thead>
        <tbody>
          @for (p of rows(); track p.id) {
            <tr [routerLink]="['/app/anagrafiche', kind, p.id]" style="cursor:pointer">
              <td><b>{{ p.name }}</b></td>
              <td>{{ p.city }}</td>
              <td>{{ p.vatNumber || '—' }}</td>
              <td>{{ p.email }}</td>
              <td>{{ p.paymentTerm }}</td>
            </tr>
          }
        </tbody>
      </table>
      @if (!rows().length) { <div class="empty"><h3>Nessun nominativo</h3><p>Crea la prima anagrafica per partire.</p></div> }
    </div>
  `
})
export class PartyListComponent {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  kind = 'CUSTOMER';
  title = 'Clienti';
  newPath = '/app/anagrafiche/clienti/new';
  rows = signal<Party[]>([]);

  constructor() {
    this.route.data.subscribe(d => {
      this.kind = d['kind'];
      this.title = d['title'];
      this.newPath = d['newPath'];
      this.api.parties(this.kind).subscribe(r => this.rows.set(r));
    });
  }
}
