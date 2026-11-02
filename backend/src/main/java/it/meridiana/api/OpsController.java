package it.meridiana.api;

import it.meridiana.common.ApiException;
import it.meridiana.domain.Company;
import it.meridiana.domain.Lookup;
import it.meridiana.domain.Warehouse;
import it.meridiana.domain.WorkEvent;
import it.meridiana.domain.WorkKind;
import it.meridiana.repo.CompanyRepository;
import it.meridiana.repo.InstallmentRepository;
import it.meridiana.repo.ItemRepository;
import it.meridiana.repo.LedgerMoveRepository;
import it.meridiana.repo.LookupRepository;
import it.meridiana.repo.PartyRepository;
import it.meridiana.repo.StockMoveRepository;
import it.meridiana.repo.WarehouseRepository;
import it.meridiana.repo.WorkEventRepository;
import it.meridiana.service.DashboardService;
import it.meridiana.service.DocumentService;
import it.meridiana.service.StatsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OpsController {
    private final DashboardService dashboard;
    private final StatsService stats;
    private final InstallmentRepository installments;
    private final DocumentService documents;
    private final LedgerMoveRepository ledger;
    private final StockMoveRepository stock;
    private final WarehouseRepository warehouses;
    private final WorkEventRepository events;
    private final CompanyRepository companies;
    private final LookupRepository lookups;
    private final PartyRepository parties;
    private final ItemRepository items;

    public OpsController(
            DashboardService dashboard,
            StatsService stats,
            InstallmentRepository installments,
            DocumentService documents,
            LedgerMoveRepository ledger,
            StockMoveRepository stock,
            WarehouseRepository warehouses,
            WorkEventRepository events,
            CompanyRepository companies,
            LookupRepository lookups,
            PartyRepository parties,
            ItemRepository items
    ) {
        this.dashboard = dashboard;
        this.stats = stats;
        this.installments = installments;
        this.documents = documents;
        this.ledger = ledger;
        this.stock = stock;
        this.warehouses = warehouses;
        this.events = events;
        this.companies = companies;
        this.lookups = lookups;
        this.parties = parties;
        this.items = items;
    }

    @GetMapping("/dashboard")
    public Api.DashboardView dashboard() {
        return dashboard.load();
    }

    @GetMapping("/stats")
    public Api.StatsView stats(@RequestParam(defaultValue = "2026") int year) {
        return stats.load(year);
    }

    @GetMapping("/installments")
    public List<Api.InstallmentView> installments(@RequestParam boolean receivable) {
        return installments.findByReceivableOrderByDueDateAsc(receivable).stream().map(Mapper::installment).toList();
    }

    @PostMapping("/installments/{id}/pay")
    public Api.InstallmentView pay(@PathVariable String id, @RequestBody Api.PayRequest req) {
        return documents.pay(id, req);
    }

    @PostMapping("/installments/{id}/remind")
    public Api.InstallmentView remind(@PathVariable String id) {
        var inst = installments.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Scadenza non trovata"));
        inst.setReminderSent(true);
        return Mapper.installment(installments.save(inst));
    }

    @GetMapping("/ledger")
    public List<Api.LedgerView> ledger() {
        return ledger.findAllByOrderByDateDescCreatedAtDesc().stream().map(Mapper::ledger).toList();
    }

    @PostMapping("/ledger")
    public Api.LedgerView addLedger(@RequestBody Api.LedgerView view) {
        var m = new it.meridiana.domain.LedgerMove();
        m.setDate(view.date() == null ? LocalDate.now() : view.date());
        m.setAccount(view.account());
        m.setCounterpart(view.counterpart());
        m.setDescription(view.description());
        m.setIncome(Mapper.nz(view.income()));
        m.setExpense(Mapper.nz(view.expense()));
        return Mapper.ledger(ledger.save(m));
    }

    @GetMapping("/stock")
    public List<Api.StockView> stock() {
        return stock.findAllByOrderByDateDescCreatedAtDesc().stream().map(Mapper::stock).toList();
    }

    @GetMapping("/warehouses")
    public List<Warehouse> warehouses() {
        return warehouses.findAll();
    }

    @PostMapping("/warehouses")
    public Warehouse saveWarehouse(@RequestBody Warehouse w) {
        return warehouses.save(w);
    }

    @GetMapping("/events")
    public List<Api.WorkView> events(@RequestParam(required = false) WorkKind kind) {
        var list = kind == null ? events.findAllByOrderByStartAtAsc() : events.findByKindOrderByStartAtDesc(kind);
        return list.stream().map(Mapper::work).toList();
    }

    @PostMapping("/events")
    public Api.WorkView saveEvent(@RequestBody Api.WorkView view) {
        WorkEvent e = view.id() == null ? new WorkEvent() : events.findById(view.id()).orElse(new WorkEvent());
        e.setKind(view.kind());
        e.setTitle(view.title());
        e.setCustomerName(view.customerName());
        e.setTechnicianName(view.technicianName());
        e.setStartAt(view.startAt());
        e.setEndAt(view.endAt());
        e.setStatus(view.status());
        e.setNotes(view.notes());
        return Mapper.work(events.save(e));
    }

    @DeleteMapping("/events/{id}")
    public void deleteEvent(@PathVariable String id) {
        events.deleteById(id);
    }

    @GetMapping("/company")
    public Api.CompanyView company() {
        return companies.findAll().stream().findFirst()
                .map(Mapper::company)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Azienda non configurata"));
    }

    @PutMapping("/company")
    public Api.CompanyView updateCompany(@RequestBody Api.CompanyView view) {
        Company c = companies.findAll().stream().findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Azienda non configurata"));
        Mapper.copyCompany(c, view);
        return Mapper.company(companies.save(c));
    }

    @GetMapping("/lookups")
    public List<Lookup> lookups(@RequestParam String group) {
        return lookups.findByGroupNameOrderByLabelAsc(group);
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String q) {
        String query = q == null ? "" : q.toLowerCase();
        var partyHits = parties.findAll().stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(query))
                .limit(8)
                .map(Mapper::party)
                .toList();
        var itemHits = items.findAll().stream()
                .filter(i -> (i.getName() != null && i.getName().toLowerCase().contains(query))
                        || (i.getCode() != null && i.getCode().toLowerCase().contains(query)))
                .limit(8)
                .map(Mapper::item)
                .toList();
        return Map.of("parties", partyHits, "items", itemHits);
    }
}
