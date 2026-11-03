import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { apiError } from '../core/errors';
import { Loan } from '../core/models';
import { day, euro } from '../core/money';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="hero">
      <div>
        <h1>Indebitamento e capacità di rimborso</h1>
        <p>Il DSCR confronta il margine operativo dell’anno (vendite − acquisti) con le rate annuali di mutui e leasing.</p>
      </div>
    </div>
    @if (error) { <p class="muted" style="color:#d32f2f;margin:0">{{ error }}</p> }
    <div class="kpis">
      <div class="kpi debt"><span>Debito residuo</span><strong>{{ euro(total('remaining')) }}</strong></div>
      <div class="kpi"><span>Servizio del debito / anno</span><strong>{{ euro(service()) }}</strong></div>
      <div class="kpi"><span>DSCR</span><strong>{{ dscr() }}</strong>
        <em>soglia di attenzione 1,25</em>
      </div>
      <div class="kpi"><span>Giudizio</span><strong style="font-size:16px">{{ headline() }}</strong></div>
    </div>
    <div class="panel">
      <h2>{{ draft.id ? 'Modifica finanziamento' : 'Nuovo finanziamento' }}</h2>
      <div class="form-grid">
        <div class="field"><label>Banca / istituto</label><input [(ngModel)]="draft.bank" name="bank"></div>
        <div class="field"><label>Finalità</label>
          <select [(ngModel)]="draft.purpose" name="purpose">
            <option>Mutuo chirografario liquidità</option>
            <option>Mutuo ipotecario</option>
            <option>Prestito agrario</option>
            <option>Leasing automezzi</option>
            <option>Leasing macchinari</option>
            <option>Credito di esercizio</option>
            <option>Altro</option>
          </select>
        </div>
        <div class="field"><label>Capitale €</label><input type="number" [(ngModel)]="draft.principal" name="prin"></div>
        <div class="field"><label>Tasso annuo %</label><input type="number" step="0.01" [(ngModel)]="draft.annualRatePct" name="rate"></div>
        <div class="field"><label>Data erogazione</label><input type="date" [(ngModel)]="draft.startDate" name="start"></div>
        <div class="field"><label>Durata (mesi)</label><input type="number" [(ngModel)]="draft.durationMonths" name="dur"></div>
        <div class="field"><label>Rata mensile € (vuoto = calcolo francese)</label><input type="number" [(ngModel)]="draft.installment" name="inst"></div>
        <div class="field"><label>Già rimborsato €</label><input type="number" [(ngModel)]="draft.paid" name="paid"></div>
      </div>
      <div class="filters" style="margin-top:12px">
        <button class="btn accent" (click)="save()">{{ draft.id ? 'Salva' : 'Registra finanziamento' }}</button>
        @if (draft.id) { <button class="btn ghost" (click)="reset()">Annulla</button> }
      </div>
    </div>
    <div class="table-card">
      <table class="table">
        <thead>
          <tr>
            <th>Finanziamento</th><th>Capitale</th><th>Tasso</th><th>Rata</th>
            <th>Residuo</th><th>Servizio/anno</th><th>Giudizio</th><th></th>
          </tr>
        </thead>
        <tbody>
          @for (l of rows(); track l.id) {
            <tr>
              <td>
                <b>{{ l.bank }}</b>
                <div class="muted">{{ l.purpose }} · {{ day(l.startDate) }} · {{ l.durationMonths }} mesi</div>
              </td>
              <td>{{ euro(l.principal) }}</td>
              <td>{{ l.annualRatePct }}%</td>
              <td>{{ euro(l.installment) }}</td>
              <td>{{ euro(l.remaining) }}</td>
              <td>{{ euro(l.annualDebtService) }}</td>
              <td><span class="chip {{ l.verdictLevel }}">{{ label(l.verdictLevel) }}</span></td>
              <td class="row-actions">
                @if ((l.remaining || 0) > 0) {
                  <button class="btn small accent" (click)="pay(l)">Paga rata</button>
                }
                <button class="btn ghost small" (click)="edit(l)">Apri</button>
                <button class="btn ghost small" (click)="remove(l)">Elimina</button>
              </td>
            </tr>
          }
        </tbody>
      </table>
      @if (!rows().length) {
        <div class="empty"><h3>Nessun finanziamento</h3><p>Registra mutui, prestiti o leasing per il DSCR.</p></div>
      }
    </div>
    @if (rows().length) {
      <div class="panel">
        <h3>Come si legge</h3>
        <p class="muted" style="margin:0">
          DSCR = (fatturato confermato − acquisti confermati dell’anno) / rate annuali.
          Sopra 1,25 la capacità è adeguata; tra 1 e 1,25 va monitorata; sotto 1 le rate non sono coperte dal margine operativo.
        </p>
        <p class="muted">{{ rows()[0].verdict }}</p>
      </div>
    }
  `
})
export class FinanceDebtComponent {
  private api = inject(ApiService);
  rows = signal<Loan[]>([]);
  error = '';
  euro = euro; day = day;
  draft: Loan = this.empty();

  constructor() { this.load(); }
  empty(): Loan {
    return { bank: '', purpose: 'Mutuo chirografario liquidità', principal: 0, annualRatePct: 4, startDate: new Date().toISOString().slice(0, 10), durationMonths: 60, installment: 0, paid: 0 };
  }
  load() {
    this.api.loans().subscribe({ next: r => this.rows.set(r), error: e => this.error = apiError(e) });
  }
  total(key: 'remaining' | 'annualDebtService') {
    return this.rows().reduce((s, l) => s + Number(l[key] || 0), 0);
  }
  service() { return this.total('annualDebtService'); }
  dscr() {
    return this.rows()[0]?.dscr != null ? Number(this.rows()[0].dscr).toFixed(2) : '—';
  }
  headline() {
    const l = this.rows()[0]?.verdictLevel;
    return this.label(l);
  }
  label(level?: string) {
    return level === 'ok' ? 'Adeguata' : level === 'warn' ? 'Da monitorare' : level === 'bad' ? 'Insufficiente' : '—';
  }
  edit(l: Loan) { this.draft = { ...l }; }
  reset() { this.draft = this.empty(); }
  save() {
    this.error = '';
    if (!this.draft.bank?.trim()) { this.error = 'Inserisci la banca.'; return; }
    this.api.saveLoan(this.draft).subscribe({
      next: () => { this.reset(); this.load(); },
      error: e => this.error = apiError(e)
    });
  }
  pay(l: Loan) {
    if (!l.id) return;
    const amount = Number(l.installment || l.remaining || 0);
    this.api.payLoan(l.id, amount).subscribe({ next: () => this.load(), error: e => this.error = apiError(e) });
  }
  remove(l: Loan) {
    if (!l.id) return;
    this.api.deleteLoan(l.id).subscribe({ next: () => this.load(), error: e => this.error = apiError(e) });
  }
}
