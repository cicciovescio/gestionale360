package it.meridiana.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "doc")
public class Doc extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private DocumentType type;
    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.DRAFT;
    @Enumerated(EnumType.STRING)
    private EinvoiceStatus einvoiceStatus = EinvoiceStatus.NOT_APPLICABLE;
    private String number;
    private String series = "A";
    private LocalDate date = LocalDate.now();
    private String partyId;
    private String partyName;
    @Enumerated(EnumType.STRING)
    private PaymentTerm paymentTerm = PaymentTerm.D30;
    private String warehouseId;
    private boolean skipStock;
    private String agentName;
    private String convertedFromId;
    @jakarta.persistence.Column(columnDefinition = "text")
    private String notes;
    private BigDecimal taxable = BigDecimal.ZERO;
    private BigDecimal tax = BigDecimal.ZERO;
    private BigDecimal stampDuty = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal paid = BigDecimal.ZERO;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    @OrderBy("lineNo")
    private List<DocLine> lines = new ArrayList<>();
}
