import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Party } from '../core/models';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="hero">
      <div>
        <h1>{{ model.name || 'Nuova anagrafica' }}</h1>
        <p>I dati fiscali alimentano fattura elettronica, scadenze e stampa.</p>
      </div>
      <div class="filters">
        <button class="btn ghost" (click)="back()">Indietro</button>
        <button class="btn accent" (click)="save()">Salva</button>
      </div>
    </div>
    <div class="form-card panel">
      <div class="form-grid">
        <div class="field"><label>Nome / Ragione sociale</label><input [(ngModel)]="model.name" name="name"></div>
        <div class="field"><label>Forma</label>
          <select [(ngModel)]="model.legalForm" name="legalForm">
            <option>Azienda</option><option>Persona</option><option>PA</option>
          </select>
        </div>
        <div class="field"><label>Partita IVA</label><input [(ngModel)]="model.vatNumber" name="vat"></div>
        <div class="field"><label>Codice fiscale</label><input [(ngModel)]="model.fiscalCode" name="cf"></div>
        <div class="field"><label>Codice SDI / PEC</label><input [(ngModel)]="model.sdiCode" name="sdi"></div>
        <div class="field"><label>PEC</label><input [(ngModel)]="model.pec" name="pec"></div>
        <div class="field span-2"><label>Indirizzo</label><input [(ngModel)]="model.address" name="address"></div>
        <div class="field"><label>Città</label><input [(ngModel)]="model.city" name="city"></div>
        <div class="field"><label>CAP</label><input [(ngModel)]="model.zip" name="zip"></div>
        <div class="field"><label>Email</label><input [(ngModel)]="model.email" name="email"></div>
        <div class="field"><label>Telefono</label><input [(ngModel)]="model.phone" name="phone"></div>
        <div class="field"><label>Pagamento</label>
          <select [(ngModel)]="model.paymentTerm" name="pay">
            <option value="IMMEDIATE">Immediato</option>
            <option value="D30">30 giorni</option>
            <option value="D30_60">30/60</option>
            <option value="D30_60_90">30/60/90</option>
            <option value="D60">60 giorni</option>
          </select>
        </div>
        <div class="field"><label>Listino</label><input [(ngModel)]="model.priceListName" name="list"></div>
        <div class="field span-2"><label>Note</label><textarea [(ngModel)]="model.notes" name="notes"></textarea></div>
      </div>
    </div>
  `
})
export class PartyEditorComponent {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  kind = 'CUSTOMER';
  listPath = '/app/anagrafiche/clienti';
  model: Party = { kind: 'CUSTOMER', name: '', legalForm: 'Azienda', paymentTerm: 'D30', active: true };

  constructor() {
    this.route.data.subscribe(d => {
      this.kind = d['kind'];
      this.listPath = d['listPath'];
      this.model.kind = this.kind;
    });
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') {
      this.api.party(id).subscribe(p => this.model = p);
    }
  }

  save() {
    this.model.kind = this.kind;
    this.api.saveParty(this.model).subscribe(() => this.back());
  }
  back() { this.router.navigateByUrl(this.listPath); }
}
