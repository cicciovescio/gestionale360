import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { apiError } from '../core/errors';
import { CONVERT_MAP, DOC_LABELS, Doc, DocLine, Item, Party } from '../core/models';
import { euro, statusChip, statusLabel } from '../core/money';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="hero">
      <div>
        <div class="stepper">
          <span class="step" [class.on]="model.status==='DRAFT'">Bozza</span>
          <span class="step" [class.on]="model.status==='CONFIRMED' || model.status==='ACCEPTED' || model.status==='PAID'">Confermato</span>
          <span class="step" [class.on]="model.einvoiceStatus==='ACCEPTED'">SDI</span>
          <span class="step" [class.on]="model.status==='PAID'">Incassato</span>
        </div>
        <h1>{{ labels[model.type] }} {{ model.number || 'in preparazione' }}</h1>
        <p>A sinistra lavori. A destra vedi il foglio come lo vedrà il cliente.</p>
      </div>
      <div class="filters">
        <button class="btn ghost" (click)="back()">Chiudi</button>
        @if (model.status === 'DRAFT') {
          <button class="btn ghost" (click)="save(false)">Salva bozza</button>
          <button class="btn accent" (click)="save(true)">Conferma</button>
        }
        @if (model.status !== 'DRAFT' && model.id && converts().length) {
          <button class="btn" (click)="convert(converts()[0])">Converti in {{ labels[converts()[0]] }}</button>
        }
        @if (model.status === 'CONFIRMED' && feable()) {
          <button class="btn accent" (click)="sendFe()">Invia allo SDI</button>
        }
        @if (model.status !== 'DRAFT' && model.status !== 'VOIDED') {
          <button class="btn warn" (click)="voidDoc()">Storna</button>
        }
      </div>
    </div>
    @if (error) { <p class="muted" style="color:#b42318;margin:0 0 12px">{{ error }}</p> }
    <div class="grid-editor">
      <div class="panel">
        <div class="form-grid">
          <div class="field">
            <label>Cliente / fornitore</label>
            <select [(ngModel)]="model.partyId" name="party" (ngModelChange)="onParty()">
              <option [ngValue]="undefined">Seleziona</option>
              @for (p of parties(); track p.id) {
                <option [ngValue]="p.id">{{ p.name }}</option>
              }
            </select>
          </div>
          <div class="field"><label>Data</label><input type="date" [(ngModel)]="model.date" name="date"></div>
          <div class="field"><label>Sezionale</label><input [(ngModel)]="model.series" name="series"></div>
          <div class="field">
            <label>Pagamento</label>
            <select [(ngModel)]="model.paymentTerm" name="term">
              <option value="IMMEDIATE">Immediato</option>
              <option value="D30">30 giorni</option>
              <option value="D30_60">30/60</option>
              <option value="D60">60 giorni</option>
            </select>
          </div>
          <div class="field span-2"><label>Note</label><textarea [(ngModel)]="model.notes" name="notes"></textarea></div>
        </div>
        <h3 style="margin:18px 0 8px">Righe</h3>
        <table class="table">
          <thead><tr><th>Articolo</th><th>Qta</th><th>Prezzo</th><th>Sconto %</th><th>IVA</th><th></th></tr></thead>
          <tbody>
            @for (line of model.lines; track $index) {
              <tr>
                <td>
                  <select [(ngModel)]="line.itemId" [name]="'item'+$index" (ngModelChange)="onItem(line)">
                    <option [ngValue]="undefined">Descrizione libera</option>
                    @for (i of items(); track i.id) { <option [ngValue]="i.id">{{ i.code }} · {{ i.name }}</option> }
                  </select>
                  <input style="margin-top:6px;width:100%" [(ngModel)]="line.description" [name]="'desc'+$index">
                </td>
                <td><input type="number" [(ngModel)]="line.qty" [name]="'qty'+$index" style="width:72px"></td>
                <td><input type="number" [(ngModel)]="line.unitPrice" [name]="'price'+$index" style="width:90px"></td>
                <td><input type="number" [(ngModel)]="line.discountPct" [name]="'disc'+$index" style="width:70px"></td>
                <td><input type="number" [(ngModel)]="line.vatRate" [name]="'vat'+$index" style="width:70px"></td>
                <td><button class="btn ghost small" (click)="remove($index)">x</button></td>
              </tr>
            }
          </tbody>
        </table>
        <button class="btn ghost small" style="margin-top:10px" (click)="addLine()">Aggiungi riga</button>
      </div>
      <aside class="preview">
        <div class="preview-head">
          <div>
            <div class="muted">Bruno Servizi S.r.l.</div>
            <h2>{{ labels[model.type] }}</h2>
            <div class="muted">{{ model.number ? 'n. ' + model.number : 'Bozza' }} · {{ model.date }}</div>
          </div>
          <span [class]="statusChip(model.status)">{{ statusLabel(model.status) }}</span>
        </div>
        <div>
          <div class="muted">Destinatario</div>
          <b>{{ partyName() || '—' }}</b>
        </div>
        <table class="table">
          <thead><tr><th>Descrizione</th><th>Qta</th><th>Importo</th></tr></thead>
          <tbody>
            @for (line of model.lines; track $index) {
              <tr>
                <td>{{ line.description }}</td>
                <td>{{ line.qty }}</td>
                <td>{{ euro(lineTotal(line)) }}</td>
              </tr>
            }
          </tbody>
        </table>
        <div class="totals">
          <div><span>Imponibile</span><span>{{ euro(totals().taxable) }}</span></div>
          <div><span>IVA</span><span>{{ euro(totals().tax) }}</span></div>
          <div><strong>Totale</strong><strong>{{ euro(totals().total) }}</strong></div>
        </div>
      </aside>
    </div>
  `
})
export class DocumentEditorComponent {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  labels = DOC_LABELS;
  euro = euro; statusChip = statusChip; statusLabel = statusLabel;
  parties = signal<Party[]>([]);
  items = signal<Item[]>([]);
  model: Doc = this.empty('SALE_INVOICE');
  listPath = '/app/vendite/fatture';
  error = '';

  converts = computed(() => CONVERT_MAP[this.model.type] || []);

  constructor() {
    this.api.items().subscribe(r => this.items.set(r));
    const id = this.route.snapshot.paramMap.get('id');
    this.route.data.subscribe(d => {
      if (d['type']) {
        this.model.type = d['type'];
        this.listPath = d['listPath'] || this.listPath;
      }
      const kind = this.model.type.startsWith('PURCHASE') ? 'SUPPLIER' : 'CUSTOMER';
      this.api.parties(kind).subscribe(r => this.parties.set(r));
    });
    if (id) {
      this.api.document(id).subscribe(doc => {
        this.model = doc;
        this.listPath = this.pathFor(doc.type);
      });
    }
  }

  empty(type: string): Doc {
    return { type, status: 'DRAFT', series: 'A', date: new Date().toISOString().slice(0, 10), paymentTerm: 'D30', skipStock: false, lines: [this.blank()] };
  }
  blank(): DocLine { return { lineKind: 'ITEM', qty: 1, unitPrice: 0, discountPct: 0, vatRate: 22, description: '' }; }
  addLine() { this.model.lines = [...(this.model.lines || []), this.blank()]; }
  remove(i: number) { this.model.lines = this.model.lines!.filter((_, idx) => idx !== i); }
  onParty() {
    const p = this.parties().find(x => x.id === this.model.partyId);
    if (p) { this.model.partyName = p.name; this.model.paymentTerm = p.paymentTerm; }
  }
  onItem(line: DocLine) {
    const item = this.items().find(i => i.id === line.itemId);
    if (item) {
      line.code = item.code;
      line.description = item.name;
      line.unitPrice = item.listPrice;
      line.vatRate = item.vatRate;
      line.unit = item.unit;
    }
  }
  partyName() { return this.parties().find(p => p.id === this.model.partyId)?.name || this.model.partyName; }
  lineTotal(line: DocLine) {
    const gross = Number(line.qty || 0) * Number(line.unitPrice || 0);
    const net = gross * (1 - Number(line.discountPct || 0) / 100);
    return net * (1 + Number(line.vatRate || 0) / 100);
  }
  totals() {
    const taxable = (this.model.lines || []).reduce((s, l) => s + Number(l.qty || 0) * Number(l.unitPrice || 0) * (1 - Number(l.discountPct || 0) / 100), 0);
    const tax = (this.model.lines || []).reduce((s, l) => {
      const net = Number(l.qty || 0) * Number(l.unitPrice || 0) * (1 - Number(l.discountPct || 0) / 100);
      return s + net * Number(l.vatRate || 0) / 100;
    }, 0);
    return { taxable, tax, total: taxable + tax };
  }
  feable() { return ['SALE_INVOICE', 'SALE_CREDIT_NOTE', 'SALE_SELF_INVOICE'].includes(this.model.type); }
  save(confirm: boolean) {
    this.error = '';
    if (!this.model.partyId) {
      this.error = 'Seleziona un cliente o fornitore.';
      return;
    }
    this.api.saveDocument(this.model).subscribe({
      next: doc => {
        this.model = doc;
        if (confirm && doc.id) {
          this.api.confirm(doc.id).subscribe({
            next: d => { this.model = d; this.back(); },
            error: e => this.error = apiError(e)
          });
        } else {
          this.back();
        }
      },
      error: e => this.error = apiError(e)
    });
  }
  convert(type: string) { if (this.model.id) this.api.convert(this.model.id, type).subscribe(d => this.router.navigate(['/app/documenti', d.id])); }
  sendFe() { if (this.model.id) this.api.einvoice(this.model.id).subscribe(d => this.model = d); }
  voidDoc() { if (this.model.id) this.api.voidDoc(this.model.id).subscribe(d => this.model = d); }
  back() { this.router.navigateByUrl(this.listPath); }
  pathFor(type: string) {
    const map: Record<string, string> = {
      SALE_QUOTE: '/app/vendite/offerte', SALE_ORDER: '/app/vendite/ordini', SALE_DDT: '/app/vendite/ddt',
      SALE_PROFORMA: '/app/vendite/proforma', SALE_INVOICE: '/app/vendite/fatture', SALE_CREDIT_NOTE: '/app/vendite/note-credito',
      SALE_SELF_INVOICE: '/app/vendite/autofatture', SALE_RECEIPT: '/app/vendite/corrispettivi',
      PURCHASE_ORDER: '/app/acquisti/ordini', PURCHASE_DDT: '/app/acquisti/ddt', PURCHASE_INVOICE: '/app/acquisti/fatture',
      PURCHASE_CREDIT_NOTE: '/app/acquisti/note-credito'
    };
    return map[type] || '/app/dashboard';
  }
}
