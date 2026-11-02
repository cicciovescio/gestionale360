package it.meridiana.service;

import it.meridiana.api.Api;
import it.meridiana.domain.Doc;
import it.meridiana.domain.DocumentStatus;
import it.meridiana.domain.DocumentType;
import it.meridiana.repo.DocRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class StatsService {
    private final DocRepository docs;

    public StatsService(DocRepository docs) {
        this.docs = docs;
    }

    public Api.StatsView load(int year) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        List<Doc> invoices = docs.findByTypeInAndDateBetween(List.of(DocumentType.SALE_INVOICE), from, to)
                .stream().filter(this::posted).toList();
        List<Api.MonthPoint> monthly = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            LocalDate start = LocalDate.of(year, i, 1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            BigDecimal sales = invoices.stream()
                    .filter(d -> !d.getDate().isBefore(start) && !d.getDate().isAfter(end))
                    .map(Doc::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal purchases = docs.findByTypeInAndDateBetween(List.of(DocumentType.PURCHASE_INVOICE), start, end)
                    .stream().filter(this::posted).map(Doc::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            monthly.add(new Api.MonthPoint(start.getMonth().getDisplayName(TextStyle.SHORT, Locale.ITALIAN), sales, purchases));
        }
        Map<String, BigDecimal> byCust = new HashMap<>();
        Map<String, BigDecimal> byItem = new HashMap<>();
        Map<String, BigDecimal> vat = new HashMap<>();
        for (Doc doc : invoices) {
            byCust.merge(doc.getPartyName(), doc.getTotal(), BigDecimal::add);
            vat.merge("Imponibile", doc.getTaxable(), BigDecimal::add);
            vat.merge("IVA", doc.getTax(), BigDecimal::add);
            doc.getLines().forEach(l -> byItem.merge(l.getDescription(), l.getTotal(), BigDecimal::add));
        }
        return new Api.StatsView(
                monthly,
                rank(byCust),
                rank(byItem),
                vat.entrySet().stream().map(e -> Map.<String, Object>of("label", e.getKey(), "total", e.getValue())).toList()
        );
    }

    private boolean posted(Doc d) {
        return d.getStatus() != DocumentStatus.DRAFT && d.getStatus() != DocumentStatus.VOIDED;
    }

    private List<Map<String, Object>> rank(Map<String, BigDecimal> map) {
        return map.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(8)
                .map(e -> Map.<String, Object>of("label", e.getKey(), "total", e.getValue()))
                .toList();
    }
}
