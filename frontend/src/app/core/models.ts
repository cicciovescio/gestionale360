export interface User {
  id: string;
  email: string;
  fullName: string;
  role: string;
}

export interface Party {
  id?: string;
  kind: string;
  legalForm?: string;
  name: string;
  vatNumber?: string;
  fiscalCode?: string;
  sdiCode?: string;
  pec?: string;
  address?: string;
  city?: string;
  zip?: string;
  province?: string;
  email?: string;
  phone?: string;
  iban?: string;
  paymentTerm?: string;
  priceListName?: string;
  agentName?: string;
  commissionPct?: number;
  active?: boolean;
  notes?: string;
  category?: string;
}

export interface Item {
  id?: string;
  code: string;
  name: string;
  kind?: string;
  category?: string;
  brand?: string;
  unit?: string;
  vatRate?: number;
  listPrice?: number;
  costPrice?: number;
  stockManaged?: boolean;
  stockQty?: number;
  warehouseName?: string;
  notes?: string;
  active?: boolean;
}

export interface DocLine {
  id?: string;
  lineNo?: number;
  lineKind?: string;
  itemId?: string;
  code?: string;
  description?: string;
  unit?: string;
  qty?: number;
  unitPrice?: number;
  discountPct?: number;
  vatRate?: number;
  taxable?: number;
  tax?: number;
  total?: number;
}

export interface Doc {
  id?: string;
  type: string;
  status?: string;
  einvoiceStatus?: string;
  number?: string;
  series?: string;
  date?: string;
  partyId?: string;
  partyName?: string;
  paymentTerm?: string;
  warehouseId?: string;
  skipStock?: boolean;
  agentName?: string;
  convertedFromId?: string;
  notes?: string;
  taxable?: number;
  tax?: number;
  stampDuty?: number;
  total?: number;
  paid?: number;
  lines?: DocLine[];
}

export interface Dashboard {
  receivables: number;
  payables: number;
  monthSales: number;
  monthPurchases: number;
  draftDocuments: number;
  overdue: number;
  einvoiceCredits: number;
  monthly: { month: string; sales: number; purchases: number }[];
  recentSales: Doc[];
  upcoming: Installment[];
}

export interface Installment {
  id: string;
  documentId: string;
  documentNumber: string;
  documentType: string;
  partyName: string;
  receivable: boolean;
  dueDate: string;
  amount: number;
  paid: number;
  method?: string;
  reminderSent: boolean;
  residual: number;
}

export interface Ledger {
  id?: string;
  date: string;
  account: string;
  counterpart?: string;
  description?: string;
  income?: number;
  expense?: number;
}

export interface Stock {
  id: string;
  date: string;
  warehouseName: string;
  itemName: string;
  qty: number;
  documentNumber?: string;
  reason?: string;
}

export interface Work {
  id?: string;
  kind: string;
  title: string;
  customerName?: string;
  technicianName?: string;
  startAt?: string;
  endAt?: string;
  status?: string;
  notes?: string;
}

export interface Company {
  id?: string;
  name: string;
  vatNumber?: string;
  fiscalCode?: string;
  pec?: string;
  sdiCode?: string;
  address?: string;
  city?: string;
  zip?: string;
  province?: string;
  phone?: string;
  email?: string;
  iban?: string;
  bankName?: string;
  notes?: string;
  einvoiceCredits?: number;
}

export const DOC_LABELS: Record<string, string> = {
  SALE_QUOTE: 'Offerta',
  SALE_ORDER: 'Ordine cliente',
  SALE_DDT: 'DDT cliente',
  SALE_DDT_RETURN: 'Reso cliente',
  SALE_PROFORMA: 'Fattura proforma',
  SALE_INVOICE: 'Fattura',
  SALE_CREDIT_NOTE: 'Nota credito',
  SALE_SELF_INVOICE: 'Autofattura',
  SALE_RECEIPT: 'Corrispettivo',
  PURCHASE_ORDER: 'Ordine fornitore',
  PURCHASE_DDT: 'DDT fornitore',
  PURCHASE_DDT_RETURN: 'Reso fornitore',
  PURCHASE_INVOICE: 'Fattura fornitore',
  PURCHASE_CREDIT_NOTE: 'Nota credito fornitore',
  WAREHOUSE_IN: 'Carico magazzino',
  WAREHOUSE_OUT: 'Scarico magazzino',
  WAREHOUSE_TRANSFER: 'Trasferimento'
};

export const CONVERT_MAP: Record<string, string[]> = {
  SALE_QUOTE: ['SALE_ORDER', 'SALE_INVOICE'],
  SALE_ORDER: ['SALE_DDT', 'SALE_INVOICE'],
  SALE_DDT: ['SALE_INVOICE'],
  SALE_INVOICE: ['SALE_CREDIT_NOTE'],
  PURCHASE_ORDER: ['PURCHASE_DDT', 'PURCHASE_INVOICE'],
  PURCHASE_DDT: ['PURCHASE_INVOICE'],
  PURCHASE_INVOICE: ['PURCHASE_CREDIT_NOTE']
};
