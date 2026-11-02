import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Company, Dashboard, Doc, Installment, Item, Ledger, Party, Stock, User, Work } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  login(email: string, password: string) {
    return this.http.post<{ token: string; user: User }>('/api/auth/login', { email, password });
  }

  me() { return this.http.get<User>('/api/me'); }
  dashboard() { return this.http.get<Dashboard>('/api/dashboard'); }
  stats(year = 2026) { return this.http.get<any>('/api/stats', { params: { year } }); }

  parties(kind: string) { return this.http.get<Party[]>('/api/parties', { params: { kind } }); }
  party(id: string) { return this.http.get<Party>(`/api/parties/${id}`); }
  saveParty(p: Party) { return p.id ? this.http.put<Party>(`/api/parties/${p.id}`, p) : this.http.post<Party>('/api/parties', p); }
  deleteParty(id: string) { return this.http.delete(`/api/parties/${id}`); }

  items() { return this.http.get<Item[]>('/api/items'); }
  item(id: string) { return this.http.get<Item>(`/api/items/${id}`); }
  saveItem(i: Item) { return i.id ? this.http.put<Item>(`/api/items/${i.id}`, i) : this.http.post<Item>('/api/items', i); }
  deleteItem(id: string) { return this.http.delete(`/api/items/${id}`); }

  documents(type: string) { return this.http.get<Doc[]>('/api/documents', { params: { type } }); }
  document(id: string) { return this.http.get<Doc>(`/api/documents/${id}`); }
  saveDocument(d: Doc) { return d.id ? this.http.put<Doc>(`/api/documents/${d.id}`, d) : this.http.post<Doc>('/api/documents', d); }
  confirm(id: string) { return this.http.post<Doc>(`/api/documents/${id}/confirm`, {}); }
  voidDoc(id: string) { return this.http.post<Doc>(`/api/documents/${id}/void`, {}); }
  convert(id: string, targetType: string) { return this.http.post<Doc>(`/api/documents/${id}/convert`, { targetType }); }
  einvoice(id: string) { return this.http.post<Doc>(`/api/documents/${id}/einvoice`, {}); }
  deleteDocument(id: string) { return this.http.delete(`/api/documents/${id}`); }

  installments(receivable: boolean) { return this.http.get<Installment[]>('/api/installments', { params: { receivable } }); }
  pay(id: string, amount: number, method: string) { return this.http.post<Installment>(`/api/installments/${id}/pay`, { amount, method }); }
  remind(id: string) { return this.http.post<Installment>(`/api/installments/${id}/remind`, {}); }

  ledger() { return this.http.get<Ledger[]>('/api/ledger'); }
  addLedger(m: Ledger) { return this.http.post<Ledger>('/api/ledger', m); }
  stock() { return this.http.get<Stock[]>('/api/stock'); }
  warehouses() { return this.http.get<any[]>('/api/warehouses'); }
  events(kind?: string) {
    let params = new HttpParams();
    if (kind) params = params.set('kind', kind);
    return this.http.get<Work[]>('/api/events', { params });
  }
  saveEvent(e: Work) { return this.http.post<Work>('/api/events', e); }
  company() { return this.http.get<Company>('/api/company'); }
  saveCompany(c: Company) { return this.http.put<Company>('/api/company', c); }
  search(q: string) { return this.http.get<any>('/api/search', { params: { q } }); }
}
