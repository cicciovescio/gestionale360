import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { apiError } from '../core/errors';
import { BudgetLine, PlanView } from '../core/models';
import { euro } from '../core/money';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="hero">
      <div>
        <h1>Pianificazione finanziaria</h1>
        <p>Confronta ricavi e costi previsti con il consuntivo di fatture e acquisti, mese per mese.</p>
      </div>
      <div class="filters">
        <input type="number" [(ngModel)]="year" name="year" style="width:100px;border:1px solid #e0e0e0;border-radius:4px;padding:8px 10px" (change)="load()">
      </div>
    </div>
    @if (error) { <p class="muted" style="color:#d32f2f;margin:0">{{ error }}</p> }
    <div class="kpis">
      <div class="kpi credit"><span>Ricavi piano</span><strong>{{ euro(plan()?.plannedRevenue) }}</strong></div>
      <div class="kpi debt"><span>Costi piano</span><strong>{{ euro(plan()?.plannedCost) }}</strong></div>
      <div class="kpi"><span>Utile piano</span><strong>{{ euro(plan()?.plannedNet) }}</strong></div>
      <div class="kpi"><span>Utile consuntivo</span><strong>{{ euro(plan()?.actualNet) }}</strong>
        <em>scostamento {{ euro(scostamento()) }}</em>
      </div>
    </div>
    <div class="panel">
      <h2>Nuova voce di piano</h2>
      <div class="form-grid">
        <div class="field"><label>Mese</label>
          <select [(ngModel)]="draft.month" name="month">
            @for (m of months; track m.n) { <option [ngValue]="m.n">{{ m.l }}</option> }
          </select>
        </div>
        <div class="field"><label>Tipo</label>
          <select [(ngModel)]="draft.kind" name="kind">
            <option value="REVENUE">Ricavo</option>
            <option value="COST">Costo</option>
          </select>
        </div>
        <div class="field"><label>Voce</label>
          <input [(ngModel)]="draft.category" name="cat" list="cats" placeholder="Es. Energia">
          <datalist id="cats">
            <option value="Vendite"></option><option value="Canoni e servizi"></option><option value="Contributi"></option>
            <option value="Materie prime"></option><option value="Manodopera"></option><option value="Energia"></option>
            <option value="Affitti e noleggi"></option><option value="Manutenzione"></option>
          </datalist>
        </div>
        <div class="field"><label>Importo €</label><input type="number" [(ngModel)]="draft.amount" name="amount"></div>
      </div>
      <button class="btn accent" style="margin-top:12px" (click)="add()">Aggiungi al piano</button>
    </div>
    <div class="table-card">
      <table class="table">
        <thead>
          <tr>
            <th>Mese</th><th>Ricavi piano</th><th>Ricavi cons.</th>
            <th>Costi piano</th><th>Costi cons.</th><th>Utile piano</th><th>Utile cons.</th><th>Scostamento</th>
          </tr>
        </thead>
        <tbody>
          @for (m of plan()?.months || []; track m.month) {
            <tr>
              <td><b>{{ m.label }}</b></td>
              <td>{{ euro(m.plannedRevenue) }}</td>
              <td>{{ euro(m.actualRevenue) }}</td>
              <td>{{ euro(m.plannedCost) }}</td>
              <td>{{ euro(m.actualCost) }}</td>
              <td>{{ euro(m.plannedNet) }}</td>
              <td>{{ euro(m.actualNet) }}</td>
              <td><span [class]="m.variance >= 0 ? 'chip ok' : 'chip bad'">{{ euro(m.variance) }}</span></td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `
})
export class FinancePlanComponent {
  private api = inject(ApiService);
  year = new Date().getFullYear();
  plan = signal<PlanView | null>(null);
  error = '';
  euro = euro;
  months = [
    { n: 1, l: 'Gennaio' }, { n: 2, l: 'Febbraio' }, { n: 3, l: 'Marzo' }, { n: 4, l: 'Aprile' },
    { n: 5, l: 'Maggio' }, { n: 6, l: 'Giugno' }, { n: 7, l: 'Luglio' }, { n: 8, l: 'Agosto' },
    { n: 9, l: 'Settembre' }, { n: 10, l: 'Ottobre' }, { n: 11, l: 'Novembre' }, { n: 12, l: 'Dicembre' }
  ];
  draft: BudgetLine = { year: this.year, month: new Date().getMonth() + 1, kind: 'REVENUE', category: 'Vendite', amount: 0 };

  constructor() { this.load(); }

  load() {
    this.error = '';
    this.draft.year = this.year;
    this.api.financePlan(this.year).subscribe({
      next: p => this.plan.set(p),
      error: e => this.error = apiError(e, 'Piano non caricato')
    });
  }
  scostamento() {
    const p = this.plan();
    return Number(p?.actualNet || 0) - Number(p?.plannedNet || 0);
  }
  add() {
    this.error = '';
    this.draft.year = this.year;
    this.api.saveBudget(this.draft).subscribe({
      next: () => this.load(),
      error: e => this.error = apiError(e)
    });
  }
}
