package it.meridiana.api;

import it.meridiana.service.FinanceService;
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

@RestController
@RequestMapping("/api/finance")
public class FinanceController {
    private final FinanceService finance;

    public FinanceController(FinanceService finance) {
        this.finance = finance;
    }

    @GetMapping("/overview")
    public Api.FinanceOverview overview(@RequestParam(required = false) Integer year) {
        return finance.overview(year == null ? LocalDate.now().getYear() : year);
    }

    @GetMapping("/plan")
    public Api.PlanView plan(@RequestParam(required = false) Integer year) {
        return finance.plan(year == null ? LocalDate.now().getYear() : year);
    }

    @PostMapping("/plan")
    public Api.BudgetEntryView createPlan(@RequestBody Api.BudgetEntryView view) {
        return finance.saveBudget(view);
    }

    @PutMapping("/plan/{id}")
    public Api.BudgetEntryView updatePlan(@PathVariable String id, @RequestBody Api.BudgetEntryView view) {
        return finance.saveBudget(new Api.BudgetEntryView(
                id, view.year(), view.month(), view.kind(), view.category(), view.amount(), view.notes()
        ));
    }

    @DeleteMapping("/plan/{id}")
    public void deletePlan(@PathVariable String id) {
        finance.deleteBudget(id);
    }

    @GetMapping("/investments")
    public List<Api.InvestmentView> investments() {
        return finance.investments();
    }

    @PostMapping("/investments")
    public Api.InvestmentView createInvestment(@RequestBody Api.InvestmentView view) {
        return finance.saveInvestment(view);
    }

    @PutMapping("/investments/{id}")
    public Api.InvestmentView updateInvestment(@PathVariable String id, @RequestBody Api.InvestmentView view) {
        return finance.saveInvestment(new Api.InvestmentView(
                id, view.name(), view.category(), view.amount(), view.startDate(), view.usefulLifeYears(),
                view.expectedAnnualReturn(), view.salvageValue(), view.notes(),
                null, null, null, null, null
        ));
    }

    @DeleteMapping("/investments/{id}")
    public void deleteInvestment(@PathVariable String id) {
        finance.deleteInvestment(id);
    }

    @GetMapping("/loans")
    public List<Api.LoanView> loans() {
        return finance.loans();
    }

    @PostMapping("/loans")
    public Api.LoanView createLoan(@RequestBody Api.LoanView view) {
        return finance.saveLoan(view);
    }

    @PutMapping("/loans/{id}")
    public Api.LoanView updateLoan(@PathVariable String id, @RequestBody Api.LoanView view) {
        return finance.saveLoan(new Api.LoanView(
                id, view.bank(), view.purpose(), view.principal(), view.annualRatePct(), view.startDate(),
                view.durationMonths(), view.installment(), view.paid(), view.notes(),
                null, null, null, null, null
        ));
    }

    @PostMapping("/loans/{id}/pay")
    public Api.LoanView payLoan(@PathVariable String id, @RequestBody Api.PayRequest req) {
        return finance.payLoan(id, req);
    }

    @DeleteMapping("/loans/{id}")
    public void deleteLoan(@PathVariable String id) {
        finance.deleteLoan(id);
    }
}
