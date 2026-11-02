import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Item } from '../core/models';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="hero">
      <div><h1>{{ model.name || 'Nuovo articolo' }}</h1><p>Un codice, un prezzo, un’aliquota. Il resto lo fa il documento.</p></div>
      <div class="filters">
        <button class="btn ghost" (click)="router.navigateByUrl('/app/catalogo/articoli')">Indietro</button>
        <button class="btn accent" (click)="save()">Salva</button>
      </div>
    </div>
    <div class="panel form-card">
      <div class="form-grid">
        <div class="field"><label>Codice</label><input [(ngModel)]="model.code" name="code"></div>
        <div class="field"><label>Nome</label><input [(ngModel)]="model.name" name="name"></div>
        <div class="field"><label>Tipo</label>
          <select [(ngModel)]="model.kind" name="kind"><option>Prodotto</option><option>Servizio</option><option>Consumabile</option></select>
        </div>
        <div class="field"><label>Categoria</label><input [(ngModel)]="model.category" name="cat"></div>
        <div class="field"><label>Marca</label><input [(ngModel)]="model.brand" name="brand"></div>
        <div class="field"><label>Unità</label><input [(ngModel)]="model.unit" name="unit"></div>
        <div class="field"><label>IVA %</label><input type="number" [(ngModel)]="model.vatRate" name="vat"></div>
        <div class="field"><label>Prezzo listino</label><input type="number" [(ngModel)]="model.listPrice" name="price"></div>
        <div class="field"><label>Costo</label><input type="number" [(ngModel)]="model.costPrice" name="cost"></div>
        <div class="field"><label>Giacenza</label><input type="number" [(ngModel)]="model.stockQty" name="qty"></div>
        <div class="field span-2"><label>Note</label><textarea [(ngModel)]="model.notes" name="notes"></textarea></div>
      </div>
    </div>
  `
})
export class ItemEditorComponent {
  api = inject(ApiService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  model: Item = { code: '', name: '', kind: 'Prodotto', unit: 'pz', vatRate: 22, listPrice: 0, costPrice: 0, stockManaged: true, stockQty: 0, active: true };
  constructor() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') this.api.item(id).subscribe(i => this.model = i);
  }
  save() { this.api.saveItem(this.model).subscribe(() => this.router.navigateByUrl('/app/catalogo/articoli')); }
}
