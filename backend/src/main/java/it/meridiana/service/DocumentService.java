package it.meridiana.service;

import it.meridiana.api.Api;
import it.meridiana.api.Mapper;
import it.meridiana.common.ApiException;
import it.meridiana.domain.Doc;
import it.meridiana.domain.DocLine;
import it.meridiana.domain.DocumentStatus;
import it.meridiana.domain.DocumentType;
import it.meridiana.domain.EinvoiceStatus;
import it.meridiana.domain.Installment;
import it.meridiana.domain.Item;
import it.meridiana.domain.LedgerMove;
import it.meridiana.domain.Party;
import it.meridiana.domain.StockMove;
import it.meridiana.domain.Warehouse;
import it.meridiana.repo.DocRepository;
import it.meridiana.repo.InstallmentRepository;
import it.meridiana.repo.ItemRepository;
import it.meridiana.repo.LedgerMoveRepository;
import it.meridiana.repo.PartyRepository;
import it.meridiana.repo.StockMoveRepository;
import it.meridiana.repo.WarehouseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class DocumentService {
    private final DocRepository docs;
    private final PartyRepository parties;
    private final ItemRepository items;
    private final WarehouseRepository warehouses;
    private final StockMoveRepository stockMoves;
    private final InstallmentRepository installments;
    private final LedgerMoveRepository ledger;

    public DocumentService(
            DocRepository docs,
            PartyRepository parties,
            ItemRepository items,
            WarehouseRepository warehouses,
            StockMoveRepository stockMoves,
            InstallmentRepository installments,
            LedgerMoveRepository ledger
    ) {
        this.docs = docs;
        this.parties = parties;
        this.items = items;
        this.warehouses = warehouses;
        this.stockMoves = stockMoves;
        this.installments = installments;
        this.ledger = ledger;
    }

    @Transactional(readOnly = true)
    public List<Api.DocView> list(DocumentType type) {
        return docs.findByTypeOrderByDateDescNumberDesc(type).stream().map(Mapper::doc).toList();
    }

    @Transactional(readOnly = true)
    public Api.DocView get(String id) {
        return Mapper.doc(load(id));
    }

    @Transactional
    public Api.DocView create(Api.DocView view) {
        Doc doc = new Doc();
        apply(doc, view);
        recalc(doc);
        if (needsEinvoice(doc.getType())) {
            doc.setEinvoiceStatus(EinvoiceStatus.READY);
        }
        return Mapper.doc(docs.save(doc));
    }

    @Transactional
    public Api.DocView update(String id, Api.DocView view) {
        Doc doc = load(id);
        assertDraft(doc);
        apply(doc, view);
        recalc(doc);
        return Mapper.doc(docs.save(doc));
    }

    @Transactional
    public void delete(String id) {
        Doc doc = load(id);
        assertDraft(doc);
        docs.delete(doc);
    }

    @Transactional
    public Api.DocView confirm(String id) {
        Doc doc = load(id);
        assertDraft(doc);
        if (doc.getLines().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Aggiungi almeno una riga prima di confermare.");
        }
        doc.setNumber(nextNumber(doc.getType(), doc.getSeries()));
        doc.setStatus(DocumentStatus.CONFIRMED);
        moveStock(doc);
        schedule(doc);
        postLedger(doc, false);
        return Mapper.doc(docs.save(doc));
    }

    @Transactional
    public Api.DocView voidDoc(String id) {
        Doc doc = load(id);
        if (doc.getStatus() == DocumentStatus.DRAFT || doc.getStatus() == DocumentStatus.VOIDED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Il documento non puo essere stornato.");
        }
        reverseStock(doc);
        installments.findByDocumentId(doc.getId()).forEach(installments::delete);
        postLedger(doc, true);
        doc.setStatus(DocumentStatus.VOIDED);
        return Mapper.doc(docs.save(doc));
    }

    @Transactional
    public Api.DocView convert(String id, DocumentType target) {
        Doc source = load(id);
        if (source.getStatus() == DocumentStatus.DRAFT || source.getStatus() == DocumentStatus.VOIDED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Converti solo documenti confermati.");
        }
        Doc copy = new Doc();
        copy.setType(target);
        copy.setStatus(DocumentStatus.DRAFT);
        copy.setDate(LocalDate.now());
        copy.setSeries(source.getSeries());
        copy.setPartyId(source.getPartyId());
        copy.setPartyName(source.getPartyName());
        copy.setPaymentTerm(source.getPaymentTerm());
        copy.setWarehouseId(source.getWarehouseId());
        copy.setAgentName(source.getAgentName());
        copy.setConvertedFromId(source.getId());
        copy.setNotes("Convertito da " + label(source.getType()) + " " + source.getNumber());
        if (needsEinvoice(target)) {
            copy.setEinvoiceStatus(EinvoiceStatus.READY);
        }
        int n = 1;
        for (DocLine line : source.getLines()) {
            DocLine l = new DocLine();
            l.setDocument(copy);
            l.setLineNo(n++);
            l.setLineKind(line.getLineKind());
            l.setItemId(line.getItemId());
            l.setCode(line.getCode());
            l.setDescription(line.getDescription());
            l.setUnit(line.getUnit());
            l.setQty(line.getQty());
            l.setUnitPrice(line.getUnitPrice());
            l.setDiscountPct(line.getDiscountPct());
            l.setVatRate(line.getVatRate());
            copy.getLines().add(l);
        }
        recalc(copy);
        return Mapper.doc(docs.save(copy));
    }

    @Transactional
    public Api.DocView submitEinvoice(String id) {
        Doc doc = load(id);
        if (doc.getStatus() == DocumentStatus.DRAFT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Conferma il documento prima di inviarlo allo SDI.");
        }
        if (!needsEinvoice(doc.getType())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Questo documento non e una fattura elettronica.");
        }
        doc.setEinvoiceStatus(EinvoiceStatus.ACCEPTED);
        doc.setStatus(DocumentStatus.ACCEPTED);
        return Mapper.doc(docs.save(doc));
    }

    @Transactional
    public Api.InstallmentView pay(String installmentId, Api.PayRequest req) {
        Installment inst = installments.findById(installmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Scadenza non trovata"));
        BigDecimal amount = Mapper.nz(req.amount());
        inst.setPaid(inst.getPaid().add(amount));
        inst.setMethod(req.method());
        installments.save(inst);
        docs.findById(inst.getDocumentId()).ifPresent(doc -> {
            doc.setPaid(doc.getPaid().add(amount));
            if (doc.getPaid().compareTo(doc.getTotal()) >= 0) {
                doc.setStatus(DocumentStatus.PAID);
            } else {
                doc.setStatus(DocumentStatus.PARTIALLY_PAID);
            }
            docs.save(doc);
        });
        LedgerMove move = new LedgerMove();
        move.setDate(LocalDate.now());
        move.setAccount(inst.isReceivable() ? "Banca" : "Banca");
        move.setCounterpart(inst.getPartyName());
        move.setDescription((inst.isReceivable() ? "Incasso " : "Pagamento ") + inst.getDocumentNumber());
        if (inst.isReceivable()) {
            move.setIncome(amount);
        } else {
            move.setExpense(amount);
        }
        move.setDocumentId(inst.getDocumentId());
        ledger.save(move);
        return Mapper.installment(inst);
    }

    private void apply(Doc doc, Api.DocView view) {
        doc.setType(view.type());
        doc.setDate(view.date() == null ? LocalDate.now() : view.date());
        doc.setSeries(view.series() == null ? "A" : view.series());
        doc.setPartyId(view.partyId());
        if (view.partyId() != null) {
            parties.findById(view.partyId()).ifPresent(p -> {
                doc.setPartyName(p.getName());
                if (view.paymentTerm() == null) {
                    doc.setPaymentTerm(p.getPaymentTerm());
                }
            });
        }
        if (view.partyName() != null) {
            doc.setPartyName(view.partyName());
        }
        if (view.paymentTerm() != null) {
            doc.setPaymentTerm(view.paymentTerm());
        }
        doc.setWarehouseId(view.warehouseId());
        doc.setSkipStock(view.skipStock());
        doc.setAgentName(view.agentName());
        doc.setNotes(view.notes());
        doc.setStampDuty(Mapper.nz(view.stampDuty()));
        doc.getLines().clear();
        if (view.lines() != null) {
            int n = 1;
            for (Api.LineView lv : view.lines()) {
                DocLine line = new DocLine();
                line.setDocument(doc);
                line.setLineNo(n++);
                line.setLineKind(lv.lineKind() == null ? "ITEM" : lv.lineKind());
                line.setItemId(lv.itemId());
                line.setCode(lv.code());
                line.setDescription(lv.description());
                line.setUnit(lv.unit() == null ? "pz" : lv.unit());
                line.setQty(Mapper.nz(lv.qty()));
                line.setUnitPrice(Mapper.nz(lv.unitPrice()));
                line.setDiscountPct(Mapper.nz(lv.discountPct()));
                line.setVatRate(lv.vatRate() == null ? new BigDecimal("22") : lv.vatRate());
                if (lv.itemId() != null) {
                    items.findById(lv.itemId()).ifPresent(item -> fillFromItem(line, item, lv));
                }
                doc.getLines().add(line);
            }
        }
    }

    private void fillFromItem(DocLine line, Item item, Api.LineView lv) {
        if (line.getCode() == null) {
            line.setCode(item.getCode());
        }
        if (line.getDescription() == null) {
            line.setDescription(item.getName());
        }
        if (lv.unitPrice() == null) {
            line.setUnitPrice(item.getListPrice());
        }
        if (lv.vatRate() == null) {
            line.setVatRate(item.getVatRate());
        }
        line.setUnit(item.getUnit());
    }

    public void recalc(Doc doc) {
        BigDecimal taxable = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (DocLine line : doc.getLines()) {
            BigDecimal gross = line.getQty().multiply(line.getUnitPrice());
            BigDecimal discount = gross.multiply(line.getDiscountPct()).divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
            BigDecimal net = gross.subtract(discount).setScale(2, RoundingMode.HALF_UP);
            BigDecimal vat = net.multiply(line.getVatRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            line.setTaxable(net);
            line.setTax(vat);
            line.setTotal(net.add(vat));
            taxable = taxable.add(net);
            tax = tax.add(vat);
        }
        doc.setTaxable(taxable);
        doc.setTax(tax);
        doc.setTotal(taxable.add(tax).add(Mapper.nz(doc.getStampDuty())));
    }

    private void moveStock(Doc doc) {
        if (doc.isSkipStock() || !affectsStock(doc.getType())) {
            return;
        }
        BigDecimal sign = stockSign(doc.getType());
        Warehouse warehouse = resolveWarehouse(doc.getWarehouseId());
        for (DocLine line : doc.getLines()) {
            if (line.getItemId() == null) {
                continue;
            }
            Item item = items.findById(line.getItemId()).orElse(null);
            if (item == null || !item.isStockManaged()) {
                continue;
            }
            BigDecimal qty = line.getQty().multiply(sign);
            item.setStockQty(item.getStockQty().add(qty));
            items.save(item);
            StockMove move = new StockMove();
            move.setDate(doc.getDate());
            move.setWarehouseId(warehouse.getId());
            move.setWarehouseName(warehouse.getName());
            move.setItemId(item.getId());
            move.setItemName(item.getName());
            move.setQty(qty);
            move.setDocumentId(doc.getId());
            move.setDocumentNumber(doc.getNumber());
            move.setReason(label(doc.getType()));
            stockMoves.save(move);
        }
    }

    private void reverseStock(Doc doc) {
        for (StockMove move : stockMoves.findByDocumentId(doc.getId())) {
            items.findById(move.getItemId()).ifPresent(item -> {
                item.setStockQty(item.getStockQty().subtract(move.getQty()));
                items.save(item);
            });
            stockMoves.delete(move);
        }
    }

    private void schedule(Doc doc) {
        if (!createsInstallments(doc.getType())) {
            return;
        }
        List<Integer> offsets = doc.getPaymentTerm().offsets();
        BigDecimal slice = doc.getTotal().divide(BigDecimal.valueOf(offsets.size()), 2, RoundingMode.HALF_UP);
        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 0; i < offsets.size(); i++) {
            BigDecimal amount = i == offsets.size() - 1 ? doc.getTotal().subtract(allocated) : slice;
            allocated = allocated.add(amount);
            Installment inst = new Installment();
            inst.setDocumentId(doc.getId());
            inst.setDocumentNumber(doc.getNumber());
            inst.setDocumentType(doc.getType());
            inst.setPartyId(doc.getPartyId());
            inst.setPartyName(doc.getPartyName());
            inst.setReceivable(isSale(doc.getType()));
            inst.setDueDate(doc.getDate().plusDays(offsets.get(i)));
            inst.setAmount(amount);
            installments.save(inst);
        }
    }

    private void postLedger(Doc doc, boolean reversal) {
        LedgerMove move = new LedgerMove();
        move.setDate(doc.getDate());
        move.setAccount(isSale(doc.getType()) ? "Clienti" : "Fornitori");
        move.setCounterpart(doc.getPartyName());
        move.setDescription((reversal ? "Storno " : "") + label(doc.getType()) + " " + doc.getNumber());
        move.setDocumentId(doc.getId());
        BigDecimal amount = doc.getTotal();
        boolean sale = isSale(doc.getType());
        if (reversal) {
            sale = !sale;
        }
        if (doc.getType() == DocumentType.SALE_CREDIT_NOTE || doc.getType() == DocumentType.PURCHASE_CREDIT_NOTE) {
            sale = !sale;
        }
        if (sale) {
            move.setIncome(amount);
        } else {
            move.setExpense(amount);
        }
        ledger.save(move);
    }

    private Warehouse resolveWarehouse(String id) {
        if (id != null) {
            return warehouses.findById(id).orElseGet(() -> warehouses.findAll().getFirst());
        }
        return warehouses.findAll().getFirst();
    }

    private String nextNumber(DocumentType type, String series) {
        String max = docs.maxNumber(type, series);
        int n = 0;
        try {
            n = Integer.parseInt(max.replaceAll("\\D", ""));
        } catch (Exception ignored) {
            n = 0;
        }
        return String.format("%06d", n + 1);
    }

    private Doc load(String id) {
        return docs.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Documento non trovato"));
    }

    private void assertDraft(Doc doc) {
        if (doc.getStatus() != DocumentStatus.DRAFT) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo le bozze sono modificabili. Usa storno o nota credito.");
        }
    }

    public static boolean isSale(DocumentType type) {
        return type.name().startsWith("SALE");
    }

    public static boolean needsEinvoice(DocumentType type) {
        return type == DocumentType.SALE_INVOICE
                || type == DocumentType.SALE_CREDIT_NOTE
                || type == DocumentType.SALE_SELF_INVOICE
                || type == DocumentType.PURCHASE_INVOICE
                || type == DocumentType.PURCHASE_CREDIT_NOTE;
    }

    public static boolean affectsStock(DocumentType type) {
        return switch (type) {
            case SALE_DDT, SALE_INVOICE, SALE_CREDIT_NOTE, PURCHASE_DDT, PURCHASE_INVOICE, PURCHASE_CREDIT_NOTE,
                 WAREHOUSE_IN, WAREHOUSE_OUT, WAREHOUSE_TRANSFER, SALE_DDT_RETURN, PURCHASE_DDT_RETURN -> true;
            default -> false;
        };
    }

    public static boolean createsInstallments(DocumentType type) {
        return type == DocumentType.SALE_INVOICE
                || type == DocumentType.SALE_CREDIT_NOTE
                || type == DocumentType.PURCHASE_INVOICE
                || type == DocumentType.PURCHASE_CREDIT_NOTE;
    }

    public static BigDecimal stockSign(DocumentType type) {
        return switch (type) {
            case SALE_DDT, SALE_INVOICE, WAREHOUSE_OUT, PURCHASE_DDT_RETURN -> new BigDecimal("-1");
            case SALE_CREDIT_NOTE, PURCHASE_DDT, PURCHASE_INVOICE, WAREHOUSE_IN, SALE_DDT_RETURN -> BigDecimal.ONE;
            default -> BigDecimal.ZERO;
        };
    }

    public static String label(DocumentType type) {
        return switch (type) {
            case SALE_QUOTE -> "Offerta";
            case SALE_ORDER -> "Ordine cliente";
            case SALE_DDT -> "DDT cliente";
            case SALE_DDT_RETURN -> "DDT da cliente";
            case SALE_PROFORMA -> "Proforma";
            case SALE_INVOICE -> "Fattura";
            case SALE_CREDIT_NOTE -> "Nota credito";
            case SALE_SELF_INVOICE -> "Autofattura";
            case SALE_RECEIPT -> "Corrispettivo";
            case PURCHASE_ORDER -> "Ordine fornitore";
            case PURCHASE_DDT -> "DDT fornitore";
            case PURCHASE_DDT_RETURN -> "DDT a fornitore";
            case PURCHASE_INVOICE -> "Fattura fornitore";
            case PURCHASE_CREDIT_NOTE -> "NC fornitore";
            case WAREHOUSE_IN -> "Carico";
            case WAREHOUSE_OUT -> "Scarico";
            case WAREHOUSE_TRANSFER -> "Trasferimento";
        };
    }

    public Party party(String id) {
        return parties.findById(id).orElse(null);
    }
}
