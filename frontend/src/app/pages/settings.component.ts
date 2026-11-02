import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { Company } from '../core/models';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="hero">
      <div><h1>Dati azienda</h1><p>Questi campi finiscono in fattura, XML e piè di pagina. Tienili veri.</p></div>
      <button class="btn accent" (click)="save()">Salva</button>
    </div>
    @if (model) {
      <div class="panel">
        <div class="form-grid">
          <div class="field"><label>Ragione sociale</label><input [(ngModel)]="model.name" name="name"></div>
          <div class="field"><label>P.IVA</label><input [(ngModel)]="model.vatNumber" name="vat"></div>
          <div class="field"><label>Codice fiscale</label><input [(ngModel)]="model.fiscalCode" name="cf"></div>
          <div class="field"><label>Codice SDI</label><input [(ngModel)]="model.sdiCode" name="sdi"></div>
          <div class="field"><label>PEC</label><input [(ngModel)]="model.pec" name="pec"></div>
          <div class="field"><label>Email</label><input [(ngModel)]="model.email" name="email"></div>
          <div class="field span-2"><label>Indirizzo</label><input [(ngModel)]="model.address" name="address"></div>
          <div class="field"><label>Città</label><input [(ngModel)]="model.city" name="city"></div>
          <div class="field"><label>CAP</label><input [(ngModel)]="model.zip" name="zip"></div>
          <div class="field"><label>IBAN</label><input [(ngModel)]="model.iban" name="iban"></div>
          <div class="field"><label>Banca</label><input [(ngModel)]="model.bankName" name="bank"></div>
          <div class="field span-2"><label>Note</label><textarea [(ngModel)]="model.notes" name="notes"></textarea></div>
        </div>
      </div>
    }
  `
})
export class SettingsComponent {
  api = inject(ApiService);
  model: Company | null = null;
  constructor() { this.api.company().subscribe(c => this.model = c); }
  save() { if (this.model) this.api.saveCompany(this.model).subscribe(c => this.model = c); }
}
