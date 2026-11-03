package it.meridiana.service;

import it.meridiana.api.Api;
import it.meridiana.api.Mapper;
import it.meridiana.common.ApiException;
import it.meridiana.domain.BudgetEntry;
import it.meridiana.domain.BudgetKind;
import it.meridiana.domain.DocumentStatus;
import it.meridiana.domain.DocumentType;
import it.meridiana.domain.Investment;
import it.meridiana.domain.Loan;
import it.meridiana.repo.BudgetEntryRepository;
import it.meridiana.repo.DocRepository;
import it.meridiana.repo.InvestmentRepository;
import it.meridiana.repo.LoanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class FinanceService {
    private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal TWELVE = new BigDecimal("12");

    private final BudgetEntryRepository budgets;
    private final InvestmentRepository investments;
    private final LoanRepository loans;
    private final DocRepository docs;

    public FinanceService(
            BudgetEntryRepository budgets,
            InvestmentRepository investments,
            LoanRepository loans,
            DocRepository docs
    ) {
        this.budgets = budgets;
        this.investments = investments;
        this.loans = loans;
        this.docs = docs;
    }

    public Api.PlanView plan(int year) {
        List<BudgetEntry> lines = budgets.findByYearOrderByMonthAscCategoryAsc(year);
        List<Api.MonthPlan> months = new ArrayList<>();
        BigDecimal planRev = BigDecimal.ZERO;
        BigDecimal planCost = BigDecimal.ZERO;
        BigDecimal actRev = BigDecimal.ZERO;
        BigDecimal actCost = BigDecimal.ZERO;
        for (int m = 1; m <= 12; m++) {
            final int month = m;
            BigDecimal pr = sumBudget(lines, month, BudgetKind.REVENUE);
            BigDecimal pc = sumBudget(lines, month, BudgetKind.COST);
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            BigDecimal ar = actual(List.of(DocumentType.SALE_INVOICE, DocumentType.SALE_RECEIPT), start, end);
            BigDecimal ac = actual(List.of(DocumentType.PURCHASE_INVOICE), start, end);
            BigDecimal pNet = pr.subtract(pc);
            BigDecimal aNet = ar.subtract(ac);
            months.add(new Api.MonthPlan(
                    month,
                    start.getMonth().getDisplayName(TextStyle.SHORT, Locale.ITALIAN),
                    pr, pc, pNet, ar, ac, aNet, aNet.subtract(pNet)
            ));
            planRev = planRev.add(pr);
            planCost = planCost.add(pc);
            actRev = actRev.add(ar);
            actCost = actCost.add(ac);
        }
        return new Api.PlanView(
                year, planRev, planCost, planRev.subtract(planCost),
                actRev, actCost, actRev.subtract(actCost),
                months, lines.stream().map(this::budgetView).toList()
        );
    }

    @Transactional
    public Api.BudgetEntryView saveBudget(Api.BudgetEntryView view) {
        BudgetEntry e = view.id() == null ? new BudgetEntry() : budgets.findById(view.id()).orElseGet(BudgetEntry::new);
        int year = view.year() == null ? LocalDate.now().getYear() : view.year();
        int month = view.month() == null ? LocalDate.now().getMonthValue() : view.month();
        if (month < 1 || month > 12) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Il mese deve essere tra 1 e 12");
        }
        e.setYear(year);
        e.setMonth(month);
        e.setKind(view.kind() == null ? BudgetKind.REVENUE : view.kind());
        e.setCategory(view.category() == null || view.category().isBlank() ? "Generale" : view.category().trim());
        e.setAmount(Mapper.nz(view.amount()));
        e.setNotes(view.notes());
        return budgetView(budgets.save(e));
    }

    @Transactional
    public void deleteBudget(String id) {
        budgets.delete(budgets.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Voce di piano non trovata")));
    }

    public List<Api.InvestmentView> investments() {
        return investments.findAllByOrderByStartDateDesc().stream().map(this::investmentView).toList();
    }

    @Transactional
    public Api.InvestmentView saveInvestment(Api.InvestmentView view) {
        if (view.name() == null || view.name().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Nome investimento obbligatorio");
        }
        Investment i = view.id() == null ? new Investment() : investments.findById(view.id()).orElseGet(Investment::new);
        i.setName(view.name().trim());
        i.setCategory(view.category());
        i.setAmount(Mapper.nz(view.amount()));
        i.setStartDate(view.startDate() == null ? LocalDate.now() : view.startDate());
        i.setUsefulLifeYears(view.usefulLifeYears() == null || view.usefulLifeYears() < 1 ? 8 : view.usefulLifeYears());
        i.setExpectedAnnualReturn(Mapper.nz(view.expectedAnnualReturn()));
        i.setSalvageValue(Mapper.nz(view.salvageValue()));
        i.setNotes(view.notes());
        return investmentView(investments.save(i));
    }

    @Transactional
    public void deleteInvestment(String id) {
        investments.delete(investments.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Investimento non trovato")));
    }

    public List<Api.LoanView> loans() {
        BigDecimal cash = operatingCash(LocalDate.now().getYear());
        BigDecimal service = annualDebtService();
        return loans.findAllByOrderByStartDateDesc().stream().map(l -> loanView(l, cash, service)).toList();
    }

    @Transactional
    public Api.LoanView saveLoan(Api.LoanView view) {
        if (view.bank() == null || view.bank().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Banca / istituto obbligatorio");
        }
        Loan l = view.id() == null ? new Loan() : loans.findById(view.id()).orElseGet(Loan::new);
        l.setBank(view.bank().trim());
        l.setPurpose(view.purpose());
        l.setPrincipal(Mapper.nz(view.principal()));
        l.setAnnualRatePct(Mapper.nz(view.annualRatePct()));
        l.setStartDate(view.startDate() == null ? LocalDate.now() : view.startDate());
        l.setDurationMonths(view.durationMonths() == null || view.durationMonths() < 1 ? 60 : view.durationMonths());
        l.setPaid(Mapper.nz(view.paid()));
        BigDecimal installment = Mapper.nz(view.installment());
        if (installment.signum() <= 0) {
            installment = frenchInstallment(l.getPrincipal(), l.getAnnualRatePct(), l.getDurationMonths());
        }
        l.setInstallment(installment);
        l.setNotes(view.notes());
        Loan saved = loans.save(l);
        BigDecimal cash = operatingCash(LocalDate.now().getYear());
        return loanView(saved, cash, annualDebtService());
    }

    @Transactional
    public Api.LoanView payLoan(String id, Api.PayRequest req) {
        Loan l = loans.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Finanziamento non trovato"));
        l.setPaid(l.getPaid().add(Mapper.nz(req.amount())));
        Loan saved = loans.save(l);
        BigDecimal cash = operatingCash(LocalDate.now().getYear());
        return loanView(saved, cash, annualDebtService());
    }

    @Transactional
    public void deleteLoan(String id) {
        loans.delete(loans.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Finanziamento non trovato")));
    }

    public Api.FinanceOverview overview(int year) {
        Api.PlanView plan = plan(year);
        List<Api.InvestmentView> inv = investments();
        BigDecimal invested = inv.stream().map(Api.InvestmentView::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expected = inv.stream().map(Api.InvestmentView::expectedAnnualReturn).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgRoi = invested.signum() == 0 ? BigDecimal.ZERO
                : expected.multiply(HUNDRED).divide(invested, 1, RoundingMode.HALF_UP);
        long sustainable = inv.stream().filter(i -> "ok".equals(i.verdictLevel())).count();
        BigDecimal remaining = loans.findAll().stream()
                .map(l -> l.getPrincipal().subtract(l.getPaid()).max(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal service = annualDebtService();
        BigDecimal cash = operatingCash(year);
        BigDecimal dscr = service.signum() == 0 ? BigDecimal.ZERO : cash.divide(service, 2, RoundingMode.HALF_UP);
        String[] debt = debtVerdict(dscr, service);
        return new Api.FinanceOverview(
                year, plan.plannedNet(), plan.actualNet(),
                invested, expected, avgRoi, sustainable, inv.size(),
                remaining, service, dscr, debt[0], debt[1]
        );
    }

    @Transactional
    public void ensureDemo() {
        if (investments.count() > 0 || loans.count() > 0 || budgets.count() > 0) {
            return;
        }
        int year = LocalDate.now().getYear();
        String[] revenueCats = {"Vendite", "Canoni e servizi", "Contributi"};
        String[] costCats = {"Materie prime", "Manodopera", "Energia", "Affitti e noleggi", "Manutenzione"};
        BigDecimal[] revBase = {bd("14500"), bd("4200"), bd("1800")};
        BigDecimal[] costBase = {bd("5200"), bd("6100"), bd("1400"), bd("1800"), bd("900")};
        for (int m = 1; m <= 12; m++) {
            BigDecimal season = m >= 4 && m <= 9 ? bd("1.12") : bd("0.92");
            for (int i = 0; i < revenueCats.length; i++) {
                budget(year, m, BudgetKind.REVENUE, revenueCats[i], revBase[i].multiply(season).setScale(0, RoundingMode.HALF_UP));
            }
            for (int i = 0; i < costCats.length; i++) {
                budget(year, m, BudgetKind.COST, costCats[i], costBase[i].multiply(season).setScale(0, RoundingMode.HALF_UP));
            }
        }
        investment("Furgone aziendale", "Automezzi", "28000", LocalDate.of(year, 3, 1), 8, "4500", "4000",
                "Sostituzione mezzo per consegne e assistenza.");
        investment("Impianto fotovoltaico 20 kW", "Impianti", "42000", LocalDate.of(year, 1, 15), 15, "6200", "2000",
                "Riduzione costo energia e autoconsumo.");
        investment("Rinnovo officina / magazzino", "Strutture", "15000", LocalDate.of(year - 1, 9, 1), 10, "1200", "0",
                "Scaffalature, banco e illuminazione LED.");
        loan("Banca Popolare di Bari", "Mutuo chirografario liquidità", "80000", "4.20",
                LocalDate.of(year - 2, 6, 1), 84, "12000");
        loan("Credit Agricole Leasing", "Leasing automezzi", "18000", "5.50",
                LocalDate.of(year - 1, 2, 1), 36, "6500");
    }

    private void budget(int year, int month, BudgetKind kind, String category, BigDecimal amount) {
        BudgetEntry e = new BudgetEntry();
        e.setYear(year);
        e.setMonth(month);
        e.setKind(kind);
        e.setCategory(category);
        e.setAmount(amount);
        budgets.save(e);
    }

    private void investment(String name, String category, String amount, LocalDate start, int life, String ret, String salvage, String notes) {
        Investment i = new Investment();
        i.setName(name);
        i.setCategory(category);
        i.setAmount(bd(amount));
        i.setStartDate(start);
        i.setUsefulLifeYears(life);
        i.setExpectedAnnualReturn(bd(ret));
        i.setSalvageValue(bd(salvage));
        i.setNotes(notes);
        investments.save(i);
    }

    private void loan(String bank, String purpose, String principal, String rate, LocalDate start, int months, String paid) {
        Loan l = new Loan();
        l.setBank(bank);
        l.setPurpose(purpose);
        l.setPrincipal(bd(principal));
        l.setAnnualRatePct(bd(rate));
        l.setStartDate(start);
        l.setDurationMonths(months);
        l.setPaid(bd(paid));
        l.setInstallment(frenchInstallment(l.getPrincipal(), l.getAnnualRatePct(), months));
        loans.save(l);
    }

    private Api.BudgetEntryView budgetView(BudgetEntry e) {
        return new Api.BudgetEntryView(e.getId(), e.getYear(), e.getMonth(), e.getKind(), e.getCategory(), e.getAmount(), e.getNotes());
    }

    private Api.InvestmentView investmentView(Investment i) {
        BigDecimal amount = Mapper.nz(i.getAmount());
        BigDecimal annual = Mapper.nz(i.getExpectedAnnualReturn());
        BigDecimal salvage = Mapper.nz(i.getSalvageValue());
        int life = Math.max(i.getUsefulLifeYears(), 1);
        BigDecimal payback = annual.signum() <= 0 ? BigDecimal.ZERO : amount.divide(annual, 1, RoundingMode.HALF_UP);
        BigDecimal roi = amount.signum() == 0 ? BigDecimal.ZERO : annual.multiply(HUNDRED).divide(amount, 1, RoundingMode.HALF_UP);
        BigDecimal depBase = amount.subtract(salvage).max(BigDecimal.ZERO);
        BigDecimal dep = depBase.divide(BigDecimal.valueOf(life), 2, RoundingMode.HALF_UP);
        String verdict;
        String level;
        if (annual.signum() <= 0 || (payback.compareTo(BigDecimal.valueOf(life)) > 0 && roi.compareTo(bd("4")) < 0)) {
            verdict = "Non sostenibile: recupero oltre la vita utile o rendimento nullo.";
            level = "bad";
        } else if (payback.compareTo(BigDecimal.valueOf(life)) <= 0 && roi.compareTo(bd("8")) >= 0) {
            verdict = "Sostenibile: il flusso atteso copre l'investimento entro la vita utile.";
            level = "ok";
        } else {
            verdict = "Da valutare: rendimento contenuto o tempi di recupero lunghi.";
            level = "warn";
        }
        return new Api.InvestmentView(
                i.getId(), i.getName(), i.getCategory(), amount, i.getStartDate(), life, annual, salvage, i.getNotes(),
                payback, roi, dep, verdict, level
        );
    }

    private Api.LoanView loanView(Loan l, BigDecimal operatingCash, BigDecimal totalService) {
        BigDecimal remaining = l.getPrincipal().subtract(Mapper.nz(l.getPaid())).max(BigDecimal.ZERO);
        BigDecimal installment = Mapper.nz(l.getInstallment());
        if (installment.signum() <= 0) {
            installment = frenchInstallment(l.getPrincipal(), l.getAnnualRatePct(), l.getDurationMonths());
        }
        BigDecimal annual = installment.multiply(TWELVE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal dscr = totalService.signum() == 0 ? BigDecimal.ZERO : operatingCash.divide(totalService, 2, RoundingMode.HALF_UP);
        String[] v = debtVerdict(dscr, totalService);
        return new Api.LoanView(
                l.getId(), l.getBank(), l.getPurpose(), l.getPrincipal(), l.getAnnualRatePct(),
                l.getStartDate(), l.getDurationMonths(), installment, Mapper.nz(l.getPaid()), l.getNotes(),
                remaining, annual, dscr, v[0], v[1]
        );
    }

    private String[] debtVerdict(BigDecimal dscr, BigDecimal service) {
        if (service.signum() == 0) {
            return new String[]{"Nessun servizio del debito in essere.", "ok"};
        }
        if (dscr.compareTo(bd("1.25")) >= 0) {
            return new String[]{"Capacità di rimborso adeguata (DSCR " + dscr + ").", "ok"};
        }
        if (dscr.compareTo(BigDecimal.ONE) >= 0) {
            return new String[]{"Capacità sufficiente ma da monitorare (DSCR " + dscr + ").", "warn"};
        }
        return new String[]{"Capacità di rimborso insufficiente (DSCR " + dscr + ").", "bad"};
    }

    private BigDecimal frenchInstallment(BigDecimal principal, BigDecimal annualRatePct, int months) {
        if (principal.signum() <= 0 || months < 1) {
            return BigDecimal.ZERO;
        }
        if (annualRatePct.signum() <= 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }
        BigDecimal monthlyRate = annualRatePct.divide(HUNDRED, MC).divide(TWELVE, MC);
        BigDecimal onePlus = BigDecimal.ONE.add(monthlyRate, MC);
        BigDecimal pow = onePlus.pow(months, MC);
        BigDecimal num = principal.multiply(monthlyRate, MC).multiply(pow, MC);
        BigDecimal den = pow.subtract(BigDecimal.ONE, MC);
        if (den.signum() == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }
        return num.divide(den, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal annualDebtService() {
        return loans.findAll().stream()
                .map(l -> {
                    BigDecimal inst = Mapper.nz(l.getInstallment());
                    if (inst.signum() <= 0) {
                        inst = frenchInstallment(l.getPrincipal(), l.getAnnualRatePct(), l.getDurationMonths());
                    }
                    if (l.getPrincipal().subtract(Mapper.nz(l.getPaid())).signum() <= 0) {
                        return BigDecimal.ZERO;
                    }
                    return inst.multiply(TWELVE);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal operatingCash(int year) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        return actual(List.of(DocumentType.SALE_INVOICE, DocumentType.SALE_RECEIPT), from, to)
                .subtract(actual(List.of(DocumentType.PURCHASE_INVOICE), from, to));
    }

    private BigDecimal actual(List<DocumentType> types, LocalDate from, LocalDate to) {
        return docs.findByTypeInAndDateBetween(types, from, to).stream()
                .filter(d -> d.getStatus() != DocumentStatus.DRAFT && d.getStatus() != DocumentStatus.VOIDED)
                .map(d -> d.getTotal() == null ? BigDecimal.ZERO : d.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumBudget(List<BudgetEntry> lines, int month, BudgetKind kind) {
        return lines.stream()
                .filter(e -> e.getMonth() == month && e.getKind() == kind)
                .map(e -> Mapper.nz(e.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
