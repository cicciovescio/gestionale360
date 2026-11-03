import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { apiError } from '../core/errors';
import { Investment } from '../core/models';
import { day, euro } from '../core/money';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="hero">
      <div>
        <h1>Sostenibilità degli investimenti</h1>
        <p>Payback, ROI e ammortamento lineare. Un investimento è sostenibile se il flusso atteso lo ripaga entro la vita utile.</p>
      </div>
    </div>
    @if (error) { <p class="muted" style="color:#d32f2f;margin:0">{{ error }}</p> }
    <div class="kpis">
      <div class="kpi"><span>Investito</span><strong>{{ euro(total('amount')) }}</strong></div>
      <div class="kpi credit"><span>Flusso annuo atteso</span><strong>{{ euro(total('expectedAnnualReturn')) }}</strong></div>
      <div class="kpi"><span>ROI medio</span><strong>{{ roiAvg() }}%</strong></div>
      <div class="kpi"><span>Sostenibili</span><strong>{{ okCount() }} / {{ rows().length }}</strong></div>
    </div>
    <div class="panel">
      <h2>{{ draft.id ? 'Modifica investimento' : 'Nuovo investimento' }}</h2>
      <div class="form-grid">
        <div class="field"><label>Nome</label><input [(ngModel)]="draft.name" name="name" placeholder="Es. Impianto irriguo"></div>
        <div class="field"><label>Categoria</label>
          <select [(ngModel)]="draft.category" name="cat">
            <option>Macchinari</option><option>Impianti</option><option>Automezzi</option>
            <option>Strutture</option><option>Terreni</option><option>Altro</option>
          </select>
        </div>
        <div class="field"><label>Importo €</label><input type="number" [(ngModel)]="draft.amount" name="amount"></div>
        <div class="field"><label>Data avvio</label><input type="date" [(ngModel)]="draft.startDate" name="start"></div>
        <div class="field"><label>Vita utile (anni)</label><input type="number" [(ngModel)]="draft.usefulLifeYears" name="life"></div>
        <div class="field"><label>Ricavo / risparmio annuo €</label><input type="number" [(ngModel)]="draft.expectedAnnualReturn" name="ret"></div>
        <div class="field"><label>Valore residuo €</label><input type="number" [(ngModel)]="draft.salvageValue" name="salv"></div>
        <div class="field span-2"><label>Note</label><input [(ngModel)]="draft.notes" name="notes"></div>
      </div>
      <div class="filters" style="margin-top:12px">
        <button class="btn accent" (click)="save()">{{ draft.id ? 'Salva' : 'Registra investimento' }}</button>
        @if (draft.id) { <button class="btn ghost" (click)="reset()">Annulla</button> }
      </div>
    </div>
    <div class="table-card">
      <table class="table">
        <thead>
          <tr>
            <th>Investimento</th><th>Importo</th><th>Vita</th><th>Flusso/anno</th>
            <th>Payback</th><th>ROI</th><th>Amm.to</th><th>Giudizio</th><th></th>
          </tr>
        </thead>
        <tbody>
          @for (i of rows(); track i.id) {
            <tr>
              <td>
                <b>{{ i.name }}</b>
                <div class="muted">{{ i.category }} · {{ day(i.startDate) }}</div>
              </td>
              <td>{{ euro(i.amount) }}</td>
              <td>{{ i.usefulLifeYears }} anni</td>
              <td>{{ euro(i.expectedAnnualReturn) }}</td>
              <td>{{ i.paybackYears ? i.paybackYears + ' anni' : '—' }}</td>
              <td>{{ i.roiPct }}%</td>
              <td>{{ euro(i.annualDepreciation) }}</td>
              <td><span class="chip {{ i.verdictLevel }}">{{ label(i.verdictLevel) }}</span></td>
              <td class="row-actions">
                <button class="btn ghost small" (click)="edit(i)">Apri</button>
                <button class="btn ghost small" (click)="remove(i)">Elimina</button>
              </td>
            </tr>
          }
        </tbody>
      </table>
      @if (!rows().length) {
        <div class="empty"><h3>Nessun investimento</h3><p>Registra un cespite per calcolare payback e ROI.</p></div>
      }
    </div>
    @if (rows().length) {
      <div class="panel">
        <h3>Come si legge</h3>
        <p class="muted" style="margin:0">Sostenibile se il payback è entro la vita utile e il ROI è almeno 8%. Da valutare se i tempi si allungano. Non sostenibile se il flusso atteso non copre l’investimento.</p>
      </div>
    }
  `
})
export class FinanceInvestmentsComponent {
  private api = inject(ApiService);
  rows = signal<Investment[]>([]);
  error = '';
  euro = euro; day = day;
  draft: Investment = this.empty();

  constructor() { this.load(); }
  empty(): Investment {
    return { name: '', category: 'Macchinari', amount: 0, startDate: new Date().toISOString().slice(0, 10), usefulLifeYears: 8, expectedAnnualReturn: 0, salvageValue: 0 };
  }
  load() {
    this.api.investments().subscribe({ next: r => this.rows.set(r), error: e => this.error = apiError(e) });
  }
  total(key: 'amount' | 'expectedAnnualReturn') {
    return this.rows().reduce((s, i) => s + Number(i[key] || 0), 0);
  }
  roiAvg() {
    const a = this.total('amount');
    return a ? ((this.total('expectedAnnualReturn') / a) * 100).toFixed(1) : '0.0';
  }
  okCount() { return this.rows().filter(i => i.verdictLevel === 'ok').length; }
  label(level?: string) {
    return level === 'ok' ? 'Sostenibile' : level === 'warn' ? 'Da valutare' : 'Non sostenibile';
  }
  edit(i: Investment) { this.draft = { ...i }; }
  reset() { this.draft = this.empty(); }
  save() {
    this.error = '';
    if (!this.draft.name?.trim()) { this.error = 'Inserisci il nome.'; return; }
    this.api.saveInvestment(this.draft).subscribe({
      next: () => { this.reset(); this.load(); },
      error: e => this.error = apiError(e)
    });
  }
  remove(i: Investment) {
    if (!i.id) return;
    this.api.deleteInvestment(i.id).subscribe({ next: () => this.load(), error: e => this.error = apiError(e) });
  }
}
