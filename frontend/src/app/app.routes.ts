import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { ShellComponent } from './layout/shell.component';
import { DashboardComponent } from './pages/dashboard.component';
import { DocumentEditorComponent } from './pages/document-editor.component';
import { DocumentListComponent } from './pages/document-list.component';
import { EventsComponent } from './pages/events.component';
import { ItemEditorComponent } from './pages/item-editor.component';
import { ItemListComponent } from './pages/item-list.component';
import { LedgerComponent } from './pages/ledger.component';
import { LoginComponent } from './pages/login.component';
import { PartyEditorComponent } from './pages/party-editor.component';
import { PartyListComponent } from './pages/party-list.component';
import { SettingsComponent } from './pages/settings.component';
import { StatsComponent } from './pages/stats.component';
import { StockComponent } from './pages/stock.component';
import { TreasuryComponent } from './pages/treasury.component';
import { XmlComponent } from './pages/xml.component';
import { FinancePlanComponent } from './pages/finance-plan.component';
import { FinanceInvestmentsComponent } from './pages/finance-investments.component';
import { FinanceDebtComponent } from './pages/finance-debt.component';

const party = (kind: string, title: string, slug: string) => ([
  { path: `anagrafiche/${slug}`, component: PartyListComponent, data: { kind, title, newPath: `/app/anagrafiche/${slug}/new` } },
  { path: `anagrafiche/${slug}/new`, component: PartyEditorComponent, data: { kind, listPath: `/app/anagrafiche/${slug}` } },
  { path: `anagrafiche/${slug}/:id`, component: PartyEditorComponent, data: { kind, listPath: `/app/anagrafiche/${slug}` } }
]);

const docs = (path: string, type: string, title: string) => ([
  { path, component: DocumentListComponent, data: { type, title, newPath: `/app/${path}/new` } },
  { path: `${path}/new`, component: DocumentEditorComponent, data: { type, listPath: `/app/${path}` } }
]);

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: 'app',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardComponent },
      ...party('CUSTOMER', 'Clienti', 'clienti'),
      ...party('SUPPLIER', 'Fornitori', 'fornitori'),
      ...party('TECHNICIAN', 'Tecnici', 'tecnici'),
      ...party('CARRIER', 'Vettori', 'vettori'),
      ...party('AGENT', 'Agenti', 'agenti'),
      { path: 'catalogo/articoli', component: ItemListComponent },
      { path: 'catalogo/articoli/new', component: ItemEditorComponent },
      { path: 'catalogo/articoli/:id', component: ItemEditorComponent },
      ...docs('vendite/offerte', 'SALE_QUOTE', 'Offerte'),
      ...docs('vendite/ordini', 'SALE_ORDER', 'Ordini da cliente'),
      ...docs('vendite/ddt', 'SALE_DDT', 'DDT a cliente'),
      ...docs('vendite/proforma', 'SALE_PROFORMA', 'Fatture proforma'),
      ...docs('vendite/fatture', 'SALE_INVOICE', 'Fatture a cliente'),
      ...docs('vendite/note-credito', 'SALE_CREDIT_NOTE', 'Note credito'),
      ...docs('vendite/autofatture', 'SALE_SELF_INVOICE', 'Autofatture'),
      ...docs('vendite/corrispettivi', 'SALE_RECEIPT', 'Corrispettivi'),
      ...docs('acquisti/ordini', 'PURCHASE_ORDER', 'Ordini a fornitore'),
      ...docs('acquisti/ddt', 'PURCHASE_DDT', 'DDT da fornitore'),
      ...docs('acquisti/fatture', 'PURCHASE_INVOICE', 'Fatture da fornitore'),
      ...docs('acquisti/note-credito', 'PURCHASE_CREDIT_NOTE', 'Note credito fornitore'),
      ...docs('magazzino/rettifiche', 'WAREHOUSE_OUT', 'Carichi e scarichi'),
      { path: 'magazzino/movimenti', component: StockComponent },
      { path: 'magazzino/disponibilita', component: StockComponent },
      { path: 'documenti/:id', component: DocumentEditorComponent },
      { path: 'scadenze/clienti', component: TreasuryComponent, data: { receivable: true } },
      { path: 'scadenze/fornitori', component: TreasuryComponent, data: { receivable: false } },
      { path: 'prima-nota', component: LedgerComponent },
      { path: 'statistiche', component: StatsComponent },
      { path: 'eventi/planning', component: EventsComponent },
      { path: 'finanza/piano', component: FinancePlanComponent },
      { path: 'finanza/investimenti', component: FinanceInvestmentsComponent },
      { path: 'finanza/debiti', component: FinanceDebtComponent },
      { path: 'fe', component: XmlComponent },
      { path: 'impostazioni', component: SettingsComponent }
    ]
  },
  { path: '', pathMatch: 'full', redirectTo: 'app/dashboard' },
  { path: '**', redirectTo: 'app/dashboard' }
];
