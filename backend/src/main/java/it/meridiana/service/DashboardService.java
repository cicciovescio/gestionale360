package it.meridiana.service;

import it.meridiana.api.Api;
import it.meridiana.api.Mapper;
import it.meridiana.domain.DocumentType;
import it.meridiana.domain.Installment;
import it.meridiana.repo.CompanyRepository;
import it.meridiana.repo.DocRepository;
import it.meridiana.repo.InstallmentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DashboardService {
    private final DocRepository docs;
    private final InstallmentRepository installments;
    private final CompanyRepository companies;

    public DashboardService(DocRepository docs, InstallmentRepository installments, CompanyRepository companies) {
        this.docs = docs;
        this.installments = installments;
        this.companies = companies;
    }

    public Api.DashboardView load() {
        LocalDate today = LocalDate.now();
        BigDecimal rec = residual(true);
        BigDecimal pay = residual(false);
        var monthSales = sum(List.of(DocumentType.SALE_INVOICE, DocumentType.SALE_RECEIPT), today.withDayOfMonth(1), today);
        var monthPurch = sum(List.of(DocumentType.PURCHASE_INVOICE), today.withDayOfMonth(1), today);
        int drafts = (int) docs.findAll().stream().filter(d -> d.getStatus().name().equals("DRAFT")).count();
        int overdue = (int) installments.findAll().stream()
                .filter(i -> i.getPaid().compareTo(i.getAmount()) < 0 && i.getDueDate().isBefore(today))
                .count();
        int credits = companies.findAll().stream().findFirst().map(c -> c.getEinvoiceCredits()).orElse(0);
        List<Api.MonthPoint> monthly = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            LocalDate start = LocalDate.of(today.getYear(), i, 1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            monthly.add(new Api.MonthPoint(
                    start.getMonth().getDisplayName(TextStyle.SHORT, Locale.ITALIAN),
                    sum(List.of(DocumentType.SALE_INVOICE, DocumentType.SALE_RECEIPT), start, end),
                    sum(List.of(DocumentType.PURCHASE_INVOICE), start, end)
            ));
        }
        var recent = docs.findByTypeInAndDateBetween(
                        List.of(DocumentType.SALE_INVOICE, DocumentType.SALE_QUOTE, DocumentType.SALE_ORDER),
                        today.minusMonths(6), today.plusDays(1))
                .stream().limit(8).map(Mapper::doc).toList();
        var upcoming = installments.findAll().stream()
                .filter(i -> i.getPaid().compareTo(i.getAmount()) < 0)
                .sorted((a, b) -> a.getDueDate().compareTo(b.getDueDate()))
                .limit(6)
                .map(Mapper::installment)
                .toList();
        return new Api.DashboardView(rec, pay, monthSales, monthPurch, drafts, overdue, credits, monthly, recent, upcoming);
    }

    private BigDecimal residual(boolean receivable) {
        return installments.findByReceivableOrderByDueDateAsc(receivable).stream()
                .map(i -> i.getAmount().subtract(i.getPaid()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sum(List<DocumentType> types, LocalDate from, LocalDate to) {
        return docs.findByTypeInAndDateBetween(types, from, to).stream()
                .filter(d -> d.getStatus() != it.meridiana.domain.DocumentStatus.DRAFT
                        && d.getStatus() != it.meridiana.domain.DocumentStatus.VOIDED)
                .map(d -> d.getType() == DocumentType.SALE_CREDIT_NOTE || d.getType() == DocumentType.PURCHASE_CREDIT_NOTE
                        ? d.getTotal().negate() : d.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
