export interface NavItem {
  label: string;
  path: string;
  group?: string;
}

export const NAV = [
  { label: 'Lavoro', items: [
    { label: 'Home', path: '/app/dashboard' },
    { label: 'Planning', path: '/app/eventi/planning' }
  ]},
  { label: 'Anagrafiche', items: [
    { label: 'Clienti', path: '/app/anagrafiche/clienti' },
    { label: 'Fornitori', path: '/app/anagrafiche/fornitori' },
    { label: 'Tecnici', path: '/app/anagrafiche/tecnici' },
    { label: 'Vettori', path: '/app/anagrafiche/vettori' },
    { label: 'Agenti', path: '/app/anagrafiche/agenti' }
  ]},
  { label: 'Catalogo', items: [
    { label: 'Prodotti e servizi', path: '/app/catalogo/articoli' },
    { label: 'Disponibilità', path: '/app/magazzino/disponibilita' }
  ]},
  { label: 'Vendite', items: [
    { label: 'Offerte', path: '/app/vendite/offerte' },
    { label: 'Ordini', path: '/app/vendite/ordini' },
    { label: 'DDT', path: '/app/vendite/ddt' },
    { label: 'Proforma', path: '/app/vendite/proforma' },
    { label: 'Fatture', path: '/app/vendite/fatture' },
    { label: 'Note credito', path: '/app/vendite/note-credito' },
    { label: 'Autofatture', path: '/app/vendite/autofatture' },
    { label: 'Corrispettivi', path: '/app/vendite/corrispettivi' }
  ]},
  { label: 'Acquisti', items: [
    { label: 'Ordini a fornitore', path: '/app/acquisti/ordini' },
    { label: 'DDT da fornitore', path: '/app/acquisti/ddt' },
    { label: 'Fatture da fornitore', path: '/app/acquisti/fatture' },
    { label: 'Note credito', path: '/app/acquisti/note-credito' }
  ]},
  { label: 'Magazzino', items: [
    { label: 'Movimenti', path: '/app/magazzino/movimenti' },
    { label: 'Carichi / scarichi', path: '/app/magazzino/rettifiche' }
  ]},
  { label: 'Denaro', items: [
    { label: 'Scadenze clienti', path: '/app/scadenze/clienti' },
    { label: 'Scadenze fornitori', path: '/app/scadenze/fornitori' },
    { label: 'Prima nota', path: '/app/prima-nota' },
    { label: 'Statistiche', path: '/app/statistiche' }
  ]},
  { label: 'Sostenibilità', items: [
    { label: 'Pianificazione finanziaria', path: '/app/finanza/piano' },
    { label: 'Investimenti', path: '/app/finanza/investimenti' },
    { label: 'Indebitamento', path: '/app/finanza/debiti' }
  ]},
  { label: 'Sistema', items: [
    { label: 'Fatture elettroniche', path: '/app/fe' },
    { label: 'Azienda', path: '/app/impostazioni' }
  ]}
];
