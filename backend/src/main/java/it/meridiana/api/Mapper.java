package it.meridiana.api;

import it.meridiana.domain.AppUser;
import it.meridiana.domain.Company;
import it.meridiana.domain.Doc;
import it.meridiana.domain.DocLine;
import it.meridiana.domain.Installment;
import it.meridiana.domain.Item;
import it.meridiana.domain.LedgerMove;
import it.meridiana.domain.Party;
import it.meridiana.domain.StockMove;
import it.meridiana.domain.WorkEvent;

import java.math.BigDecimal;

public final class Mapper {
    private Mapper() {
    }

    public static Api.UserView user(AppUser u) {
        return new Api.UserView(u.getId(), u.getEmail(), u.getFullName(), u.getRole());
    }

    public static Api.PartyView party(Party p) {
        return new Api.PartyView(
                p.getId(), p.getKind(), p.getLegalForm(), p.getName(), p.getVatNumber(), p.getFiscalCode(),
                p.getSdiCode(), p.getPec(), p.getAddress(), p.getCity(), p.getZip(), p.getProvince(),
                p.getEmail(), p.getPhone(), p.getIban(), p.getPaymentTerm(), p.getPriceListName(),
                p.getAgentName(), p.getCommissionPct(), p.isActive(), p.getNotes(), p.getCategory()
        );
    }

    public static void copyParty(Party p, Api.PartyView v) {
        p.setKind(v.kind());
        p.setLegalForm(v.legalForm());
        p.setName(v.name());
        p.setVatNumber(v.vatNumber());
        p.setFiscalCode(v.fiscalCode());
        p.setSdiCode(v.sdiCode());
        p.setPec(v.pec());
        p.setAddress(v.address());
        p.setCity(v.city());
        p.setZip(v.zip());
        p.setProvince(v.province());
        p.setEmail(v.email());
        p.setPhone(v.phone());
        p.setIban(v.iban());
        p.setPaymentTerm(v.paymentTerm() == null ? p.getPaymentTerm() : v.paymentTerm());
        p.setPriceListName(v.priceListName());
        p.setAgentName(v.agentName());
        p.setCommissionPct(nz(v.commissionPct()));
        p.setActive(v.active());
        p.setNotes(v.notes());
        p.setCategory(v.category());
    }

    public static Api.ItemView item(Item i) {
        return new Api.ItemView(
                i.getId(), i.getCode(), i.getName(), i.getKind(), i.getCategory(), i.getBrand(), i.getUnit(),
                i.getVatRate(), i.getListPrice(), i.getCostPrice(), i.isStockManaged(), i.getStockQty(),
                i.getWarehouseName(), i.getNotes(), i.isActive()
        );
    }

    public static void copyItem(Item i, Api.ItemView v) {
        i.setCode(v.code());
        i.setName(v.name());
        i.setKind(v.kind());
        i.setCategory(v.category());
        i.setBrand(v.brand());
        i.setUnit(v.unit() == null ? "pz" : v.unit());
        i.setVatRate(nz(v.vatRate()));
        i.setListPrice(nz(v.listPrice()));
        i.setCostPrice(nz(v.costPrice()));
        i.setStockManaged(v.stockManaged());
        if (v.stockQty() != null) {
            i.setStockQty(v.stockQty());
        }
        i.setWarehouseName(v.warehouseName());
        i.setNotes(v.notes());
        i.setActive(v.active());
    }

    public static Api.LineView line(DocLine l) {
        return new Api.LineView(
                l.getId(), l.getLineNo(), l.getLineKind(), l.getItemId(), l.getCode(), l.getDescription(),
                l.getUnit(), l.getQty(), l.getUnitPrice(), l.getDiscountPct(), l.getVatRate(),
                l.getTaxable(), l.getTax(), l.getTotal()
        );
    }

    public static Api.DocView doc(Doc d) {
        return new Api.DocView(
                d.getId(), d.getType(), d.getStatus(), d.getEinvoiceStatus(), d.getNumber(), d.getSeries(),
                d.getDate(), d.getPartyId(), d.getPartyName(), d.getPaymentTerm(), d.getWarehouseId(),
                d.isSkipStock(), d.getAgentName(), d.getConvertedFromId(), d.getNotes(), d.getTaxable(),
                d.getTax(), d.getStampDuty(), d.getTotal(), d.getPaid(),
                d.getLines().stream().map(Mapper::line).toList()
        );
    }

    public static Api.InstallmentView installment(Installment i) {
        BigDecimal residual = i.getAmount().subtract(i.getPaid());
        return new Api.InstallmentView(
                i.getId(), i.getDocumentId(), i.getDocumentNumber(), i.getDocumentType(), i.getPartyName(),
                i.isReceivable(), i.getDueDate(), i.getAmount(), i.getPaid(), i.getMethod(), i.isReminderSent(),
                residual
        );
    }

    public static Api.LedgerView ledger(LedgerMove m) {
        return new Api.LedgerView(m.getId(), m.getDate(), m.getAccount(), m.getCounterpart(), m.getDescription(),
                m.getIncome(), m.getExpense());
    }

    public static Api.StockView stock(StockMove m) {
        return new Api.StockView(m.getId(), m.getDate(), m.getWarehouseName(), m.getItemName(), m.getQty(),
                m.getDocumentNumber(), m.getReason());
    }

    public static Api.WorkView work(WorkEvent e) {
        return new Api.WorkView(e.getId(), e.getKind(), e.getTitle(), e.getCustomerName(), e.getTechnicianName(),
                e.getStartAt(), e.getEndAt(), e.getStatus(), e.getNotes());
    }

    public static Api.CompanyView company(Company c) {
        return new Api.CompanyView(c.getId(), c.getName(), c.getVatNumber(), c.getFiscalCode(), c.getPec(),
                c.getSdiCode(), c.getAddress(), c.getCity(), c.getZip(), c.getProvince(), c.getPhone(),
                c.getEmail(), c.getIban(), c.getBankName(), c.getNotes(), c.getEinvoiceCredits());
    }

    public static void copyCompany(Company c, Api.CompanyView v) {
        c.setName(v.name());
        c.setVatNumber(v.vatNumber());
        c.setFiscalCode(v.fiscalCode());
        c.setPec(v.pec());
        c.setSdiCode(v.sdiCode());
        c.setAddress(v.address());
        c.setCity(v.city());
        c.setZip(v.zip());
        c.setProvince(v.province());
        c.setPhone(v.phone());
        c.setEmail(v.email());
        c.setIban(v.iban());
        c.setBankName(v.bankName());
        c.setNotes(v.notes());
    }

    public static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
