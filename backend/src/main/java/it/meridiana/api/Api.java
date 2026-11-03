package it.meridiana.api;

import it.meridiana.domain.BudgetKind;
import it.meridiana.domain.DocumentStatus;
import it.meridiana.domain.DocumentType;
import it.meridiana.domain.EinvoiceStatus;
import it.meridiana.domain.PartyKind;
import it.meridiana.domain.PaymentTerm;
import it.meridiana.domain.Role;
import it.meridiana.domain.WorkKind;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class Api {
    private Api() {
    }

    public record LoginRequest(String email, String password) {
    }

    public record LoginResponse(String token, UserView user) {
    }

    public record UserView(String id, String email, String fullName, Role role) {
    }

    public record PartyView(
            String id, PartyKind kind, String legalForm, String name, String vatNumber, String fiscalCode,
            String sdiCode, String pec, String address, String city, String zip, String province,
            String email, String phone, String iban, PaymentTerm paymentTerm, String priceListName,
            String agentName, BigDecimal commissionPct, Boolean active, String notes, String category
    ) {
    }

    public record ItemView(
            String id, String code, String name, String kind, String category, String brand, String unit,
            BigDecimal vatRate, BigDecimal listPrice, BigDecimal costPrice, Boolean stockManaged,
            BigDecimal stockQty, String warehouseName, String notes, Boolean active
    ) {
    }

    public record LineView(
            String id, Integer lineNo, String lineKind, String itemId, String code, String description, String unit,
            BigDecimal qty, BigDecimal unitPrice, BigDecimal discountPct, BigDecimal vatRate,
            BigDecimal taxable, BigDecimal tax, BigDecimal total
    ) {
    }

    public record DocView(
            String id, DocumentType type, DocumentStatus status, EinvoiceStatus einvoiceStatus,
            String number, String series, LocalDate date, String partyId, String partyName,
            PaymentTerm paymentTerm, String warehouseId, Boolean skipStock, String agentName,
            String convertedFromId, String notes, BigDecimal taxable, BigDecimal tax, BigDecimal stampDuty,
            BigDecimal total, BigDecimal paid, List<LineView> lines
    ) {
    }

    public record ConvertRequest(DocumentType targetType) {
    }

    public record PayRequest(BigDecimal amount, String method) {
    }

    public record DashboardView(
            BigDecimal receivables,
            BigDecimal payables,
            BigDecimal monthSales,
            BigDecimal monthPurchases,
            int draftDocuments,
            int overdue,
            int einvoiceCredits,
            List<MonthPoint> monthly,
            List<DocView> recentSales,
            List<InstallmentView> upcoming
    ) {
    }

    public record MonthPoint(String month, BigDecimal sales, BigDecimal purchases) {
    }

    public record InstallmentView(
            String id, String documentId, String documentNumber, DocumentType documentType,
            String partyName, boolean receivable, LocalDate dueDate, BigDecimal amount,
            BigDecimal paid, String method, boolean reminderSent, BigDecimal residual
    ) {
    }

    public record LedgerView(
            String id, LocalDate date, String account, String counterpart, String description,
            BigDecimal income, BigDecimal expense
    ) {
    }

    public record StockView(
            String id, LocalDate date, String warehouseName, String itemName, BigDecimal qty,
            String documentNumber, String reason
    ) {
    }

    public record WorkView(
            String id, WorkKind kind, String title, String customerName, String technicianName,
            LocalDateTime startAt, LocalDateTime endAt, String status, String notes
    ) {
    }

    public record CompanyView(
            String id, String name, String vatNumber, String fiscalCode, String pec, String sdiCode,
            String address, String city, String zip, String province, String phone, String email,
            String iban, String bankName, String notes, int einvoiceCredits
    ) {
    }

    public record StatsView(
            List<MonthPoint> monthly,
            List<Map<String, Object>> byCustomer,
            List<Map<String, Object>> byItem,
            List<Map<String, Object>> vatSummary
    ) {
    }

    public record BudgetEntryView(
            String id, Integer year, Integer month, BudgetKind kind, String category,
            BigDecimal amount, String notes
    ) {
    }

    public record MonthPlan(
            int month, String label,
            BigDecimal plannedRevenue, BigDecimal plannedCost, BigDecimal plannedNet,
            BigDecimal actualRevenue, BigDecimal actualCost, BigDecimal actualNet,
            BigDecimal variance
    ) {
    }

    public record PlanView(
            int year,
            BigDecimal plannedRevenue, BigDecimal plannedCost, BigDecimal plannedNet,
            BigDecimal actualRevenue, BigDecimal actualCost, BigDecimal actualNet,
            List<MonthPlan> months,
            List<BudgetEntryView> lines
    ) {
    }

    public record InvestmentView(
            String id, String name, String category, BigDecimal amount, LocalDate startDate,
            Integer usefulLifeYears, BigDecimal expectedAnnualReturn, BigDecimal salvageValue, String notes,
            BigDecimal paybackYears, BigDecimal roiPct, BigDecimal annualDepreciation,
            String verdict, String verdictLevel
    ) {
    }

    public record LoanView(
            String id, String bank, String purpose, BigDecimal principal, BigDecimal annualRatePct,
            LocalDate startDate, Integer durationMonths, BigDecimal installment, BigDecimal paid, String notes,
            BigDecimal remaining, BigDecimal annualDebtService, BigDecimal dscr,
            String verdict, String verdictLevel
    ) {
    }

    public record FinanceOverview(
            int year,
            BigDecimal plannedNet, BigDecimal actualNet,
            BigDecimal invested, BigDecimal expectedReturn, BigDecimal avgRoi,
            long sustainableInvestments, long investmentCount,
            BigDecimal debtRemaining, BigDecimal annualDebtService, BigDecimal dscr,
            String debtVerdict, String debtLevel
    ) {
    }
}
