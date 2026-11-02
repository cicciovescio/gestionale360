package it.meridiana.service;

import it.meridiana.api.Api;
import it.meridiana.domain.AppUser;
import it.meridiana.domain.Company;
import it.meridiana.domain.DocumentType;
import it.meridiana.domain.Item;
import it.meridiana.domain.Lookup;
import it.meridiana.domain.Party;
import it.meridiana.domain.PartyKind;
import it.meridiana.domain.PaymentTerm;
import it.meridiana.domain.Role;
import it.meridiana.domain.Warehouse;
import it.meridiana.domain.WorkEvent;
import it.meridiana.domain.WorkKind;
import it.meridiana.repo.AppUserRepository;
import it.meridiana.repo.CompanyRepository;
import it.meridiana.repo.ItemRepository;
import it.meridiana.repo.LookupRepository;
import it.meridiana.repo.PartyRepository;
import it.meridiana.repo.WarehouseRepository;
import it.meridiana.repo.WorkEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SeedService implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SeedService.class);
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final CompanyRepository companies;
    private final PartyRepository parties;
    private final ItemRepository items;
    private final WarehouseRepository warehouses;
    private final LookupRepository lookups;
    private final WorkEventRepository events;
    private final DocumentService documents;

    public SeedService(
            AppUserRepository users,
            PasswordEncoder encoder,
            CompanyRepository companies,
            PartyRepository parties,
            ItemRepository items,
            WarehouseRepository warehouses,
            LookupRepository lookups,
            WorkEventRepository events,
            DocumentService documents
    ) {
        this.users = users;
        this.encoder = encoder;
        this.companies = companies;
        this.parties = parties;
        this.items = items;
        this.warehouses = warehouses;
        this.lookups = lookups;
        this.events = events;
        this.documents = documents;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ensureUser("vincenzo@mposta.it", "Vincenzo", "88888888", Role.OWNER);
        ensureUser("demo@mposta.it", "Demo", "88888888", Role.ADMIN);
        ensureUser("utente@mposta.it", "Utente", "88888888", Role.ADMIN);

        if (companies.count() > 0) {
            log.info("Database già popolato, seed demo saltato.");
            return;
        }
        log.info("Carico utenti, anagrafiche e documenti demo...");
        AppUser bruno = user("bruno@mposta.it", "Bruno Esposito", "Bruno2006@", Role.OWNER);
        user("anna@meridiana.local", "Anna Ricci", "Anna2026@", Role.ADMIN);
        user("luca@meridiana.local", "Luca Ferrara", "Luca2026@", Role.WAREHOUSE);

        Company company = new Company();
        company.setName("Bruno Servizi S.r.l.");
        company.setVatNumber("IT02488131217");
        company.setFiscalCode("02488131217");
        company.setPec("bruno.servizi@pec.it");
        company.setSdiCode("M5UXCR1");
        company.setAddress("Via dei Mille 18");
        company.setCity("Bari");
        company.setZip("70122");
        company.setProvince("BA");
        company.setPhone("080 555 1200");
        company.setEmail("bruno@mposta.it");
        company.setIban("IT60X0542811101000000123456");
        company.setBankName("Banca Popolare di Bari");
        company.setNotes("Azienda demo Meridiana. Fatturazione elettronica attiva.");
        company.setEinvoiceCredits(1486);
        companies.save(company);

        Warehouse sede = warehouse("Sede principale", "Via dei Mille 18, Bari", true);
        warehouse("Deposito Modugno", "Zona industriale, Modugno", false);

        lookup("vat", "22", "22% — ordinaria");
        lookup("vat", "10", "10% — ridotta");
        lookup("vat", "4", "4% — minima");
        lookup("vat", "0", "0% — esente");
        lookup("unit", "pz", "Pezzi");
        lookup("unit", "h", "Ore");
        lookup("unit", "kg", "Chilogrammi");
        lookup("unit", "m", "Metri");
        lookup("category-party", "HORECA", "HORECA");
        lookup("category-party", "SANITA", "Sanità");
        lookup("category-party", "PROF", "Professionisti");
        lookup("category-item", "SERV", "Servizi");
        lookup("category-item", "HW", "Hardware");
        lookup("category-item", "CONS", "Consumabili");

        Party rossi = party(PartyKind.CUSTOMER, "Azienda", "Officine Rossi S.n.c.", "IT08511240726", "M5UXCR1",
                "Via Amendola 44", "Bari", "080 334 1100", "amministrazione@officine-rossi.it", PaymentTerm.D30, "Standard", "HORECA");
        Party farmacia = party(PartyKind.CUSTOMER, "Azienda", "Farmacia San Marco", "IT01922010732", "0000000",
                "Piazza Mercantile 3", "Bari", "080 521 0091", "info@farmaciasanmarco.it", PaymentTerm.D30_60, "Sanità", "SANITA");
        Party hotel = party(PartyKind.CUSTOMER, "Azienda", "Hotel Luce Adriatica", "IT07644010724", "KRRH6B9",
                "Lungomare Nazario Sauro 12", "Bari", "080 220 4410", "hello@hotelluce.it", PaymentTerm.D30, "Hospitality", "HORECA");
        Party studio = party(PartyKind.CUSTOMER, "Azienda", "Studio Legale De Santis", "IT01299880723", "0000000",
                "Corso Cavour 90", "Bari", "080 523 7711", "studio@desantis.legal", PaymentTerm.IMMEDIATE, "Professionisti", "PROF");
        Party comune = party(PartyKind.CUSTOMER, "PA", "Comune di Mola di Bari", "IT00412210724", "UF2EH8",
                "Piazza XX Settembre 1", "Mola di Bari", "080 474 1111", "protocollo@comune.moladibari.ba.it", PaymentTerm.D60, "PA", "PA");
        Party metro = party(PartyKind.SUPPLIER, "Azienda", "Metro Ufficio S.r.l.", "IT10233410158", "M5UXCR1",
                "Via delle Officine 8", "Modugno", "080 532 9000", "ordini@metroufficio.it", PaymentTerm.D30, "Fornitori", "HW");
        Party cloud = party(PartyKind.SUPPLIER, "Azienda", "Nimbus Cloud S.p.A.", "IT06622170966", "SUBM70N",
                "Corso Italia 21", "Milano", "02 365 44100", "billing@nimbus.cloud", PaymentTerm.D30, "Fornitori", "SERV");
        party(PartyKind.TECHNICIAN, "Persona", "Marco Greco", null, null, "Via Napoli 4", "Bari", "333 120 4455",
                "marco.greco@meridiana.local", PaymentTerm.IMMEDIATE, "Tecnici", "TECH");
        party(PartyKind.TECHNICIAN, "Persona", "Sara Leone", null, null, "Via Sparano 15", "Bari", "347 880 1122",
                "sara.leone@meridiana.local", PaymentTerm.IMMEDIATE, "Tecnici", "TECH");
        party(PartyKind.CARRIER, "Azienda", "Spedire Sud", "IT03322110728", null, "S.S. 16 km 780", "Modugno",
                "080 499 2000", "logistica@spediresud.it", PaymentTerm.D30, "Logistica", "LOG");
        party(PartyKind.AGENT, "Persona", "Giulia Nardelli", "ITNRDGLI80A41A662X", "0000000", "Via Dante 7", "Bitonto",
                "349 221 0088", "giulia.nardelli@meridiana.local", PaymentTerm.D30, "Agenti", "SALES");

        Item assistenza = item("SRV-ASS", "Assistenza tecnica on-site", "Servizio", "SERV", "Meridiana", "h", "22", "48.00", "0", false, "0");
        Item canone = item("SRV-CAN", "Canone assistenza annuale", "Servizio", "SERV", "Meridiana", "pz", "22", "890.00", "0", false, "0");
        Item install = item("SRV-INS", "Installazione postazione", "Servizio", "SERV", "Meridiana", "pz", "22", "160.00", "0", false, "0");
        Item notebook = item("HW-NB14", "Notebook 14\" i5 16GB", "Prodotto", "HW", "Lenovo", "pz", "22", "980.00", "720.00", true, "12");
        Item monitor = item("HW-MON27", "Monitor 27\" USB-C", "Prodotto", "HW", "Dell", "pz", "22", "310.00", "210.00", true, "18");
        Item toner = item("CONS-TN", "Toner universale nero", "Consumabile", "CONS", "Generic", "pz", "22", "42.00", "18.50", true, "40");
        Item carta = item("CONS-A4", "Risma A4 80g 500 ff", "Consumabile", "CONS", "Burgo", "pz", "22", "4.80", "2.10", true, "120");
        Item switch8 = item("HW-SW8", "Switch gigabit 8 porte", "Prodotto", "HW", "TP-Link", "pz", "22", "58.00", "32.00", true, "9");

        LocalDate today = LocalDate.now();
        confirm(sale(DocumentType.SALE_QUOTE, rossi, today.minusDays(18), sede, line(assistenza, 8), line(toner, 4)));
        var order = confirm(sale(DocumentType.SALE_ORDER, hotel, today.minusDays(12), sede, line(notebook, 6), line(monitor, 6), line(install, 6)));
        confirm(sale(DocumentType.SALE_DDT, hotel, today.minusDays(9), sede, line(notebook, 6), line(monitor, 6)));
        var invHotel = confirm(sale(DocumentType.SALE_INVOICE, hotel, today.minusDays(8), sede, line(notebook, 6), line(monitor, 6), line(install, 6)));
        documents.submitEinvoice(invHotel.id());
        var invFarm = confirm(sale(DocumentType.SALE_INVOICE, farmacia, today.minusDays(21), sede, line(canone, 1), line(toner, 10)));
        documents.submitEinvoice(invFarm.id());
        confirm(sale(DocumentType.SALE_INVOICE, studio, today.minusDays(4), sede, line(assistenza, 12)));
        confirm(sale(DocumentType.SALE_INVOICE, comune, today.minusDays(40), sede, line(canone, 2), line(assistenza, 20)));
        documents.create(sale(DocumentType.SALE_INVOICE, rossi, today, sede, line(switch8, 2), line(carta, 20)));
        confirm(sale(DocumentType.SALE_PROFORMA, studio, today.minusDays(2), sede, line(assistenza, 6)));
        confirm(purchase(DocumentType.PURCHASE_ORDER, metro, today.minusDays(16), sede, line(notebook, 8), line(monitor, 8)));
        confirm(purchase(DocumentType.PURCHASE_DDT, metro, today.minusDays(14), sede, line(notebook, 8), line(monitor, 8)));
        confirm(purchase(DocumentType.PURCHASE_INVOICE, metro, today.minusDays(13), sede, line(notebook, 8), line(monitor, 8)));
        confirm(purchase(DocumentType.PURCHASE_INVOICE, cloud, today.minusDays(6), sede, line(canone, 1)));

        work(WorkKind.INTERVENTION, "Sostituzione switch reception", hotel.getName(), "Marco Greco",
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0), "Programmato");
        work(WorkKind.INTERVENTION, "Allineamento gestionale cassa", farmacia.getName(), "Sara Leone",
                LocalDateTime.now().minusDays(1).withHour(15).withMinute(30), "Completato");
        work(WorkKind.JOB, "Rinnovo postazioni amministrazione", rossi.getName(), "Marco Greco",
                LocalDateTime.now().plusDays(5).withHour(9).withMinute(0), "Aperta");
        work(WorkKind.HOLIDAY, "Ferie Sara Leone", "", "Sara Leone",
                LocalDateTime.now().plusDays(12).withHour(9).withMinute(0), "Approvato");
        work(WorkKind.PLANNING, "Manutenzione trimestrale Comune", comune.getName(), "Sara Leone",
                LocalDateTime.now().plusDays(3).withHour(11).withMinute(0), "Programmato");

        documents.convert(order.id(), DocumentType.SALE_INVOICE);
        bruno.setFullName("Bruno Esposito");
        users.save(bruno);
        log.info("Dati demo pronti. Login: bruno@mposta.it / Bruno2006@");
    }

    private AppUser user(String email, String name, String password, Role role) {
        return ensureUser(email, name, password, role);
    }

    private AppUser ensureUser(String email, String name, String password, Role role) {
        AppUser u = users.findByEmailIgnoreCase(email).orElseGet(AppUser::new);
        u.setEmail(email);
        u.setFullName(name);
        u.setPasswordHash(encoder.encode(password));
        u.setRole(role);
        u.setActive(true);
        AppUser saved = users.save(u);
        log.info("Utente pronto: {}", email);
        return saved;
    }

    private Warehouse warehouse(String name, String address, boolean main) {
        Warehouse w = new Warehouse();
        w.setName(name);
        w.setAddress(address);
        w.setMain(main);
        return warehouses.save(w);
    }

    private void lookup(String group, String code, String label) {
        Lookup l = new Lookup();
        l.setGroupName(group);
        l.setCode(code);
        l.setLabel(label);
        lookups.save(l);
    }

    private Party party(PartyKind kind, String form, String name, String vat, String sdi, String address, String city,
                        String phone, String email, PaymentTerm term, String list, String category) {
        Party p = new Party();
        p.setKind(kind);
        p.setLegalForm(form);
        p.setName(name);
        p.setVatNumber(vat);
        p.setSdiCode(sdi);
        p.setAddress(address);
        p.setCity(city);
        p.setZip("70100");
        p.setProvince("BA");
        p.setPhone(phone);
        p.setEmail(email);
        p.setPaymentTerm(term);
        p.setPriceListName(list);
        p.setCategory(category);
        p.setActive(true);
        return parties.save(p);
    }

    private Item item(String code, String name, String kind, String cat, String brand, String unit, String vat,
                      String price, String cost, boolean stock, String qty) {
        Item i = new Item();
        i.setCode(code);
        i.setName(name);
        i.setKind(kind);
        i.setCategory(cat);
        i.setBrand(brand);
        i.setUnit(unit);
        i.setVatRate(new BigDecimal(vat));
        i.setListPrice(new BigDecimal(price));
        i.setCostPrice(new BigDecimal(cost));
        i.setStockManaged(stock);
        i.setStockQty(new BigDecimal(qty));
        i.setWarehouseName("Sede principale");
        return items.save(i);
    }

    private Api.LineView line(Item item, int qty) {
        return new Api.LineView(null, 0, "ITEM", item.getId(), item.getCode(), item.getName(), item.getUnit(),
                BigDecimal.valueOf(qty), item.getListPrice(), BigDecimal.ZERO, item.getVatRate(),
                null, null, null);
    }

    private Api.DocView sale(DocumentType type, Party party, LocalDate date, Warehouse warehouse, Api.LineView... lines) {
        return new Api.DocView(null, type, null, null, null, "A", date, party.getId(), party.getName(),
                party.getPaymentTerm(), warehouse.getId(), false, "Giulia Nardelli", null, null,
                null, null, null, null, null, List.of(lines));
    }

    private Api.DocView purchase(DocumentType type, Party party, LocalDate date, Warehouse warehouse, Api.LineView... lines) {
        return sale(type, party, date, warehouse, lines);
    }

    private Api.DocView confirm(Api.DocView draft) {
        return documents.confirm(documents.create(draft).id());
    }

    private void work(WorkKind kind, String title, String customer, String tech, LocalDateTime start, String status) {
        WorkEvent e = new WorkEvent();
        e.setKind(kind);
        e.setTitle(title);
        e.setCustomerName(customer);
        e.setTechnicianName(tech);
        e.setStartAt(start);
        e.setEndAt(start.plusHours(2));
        e.setStatus(status);
        events.save(e);
    }
}
