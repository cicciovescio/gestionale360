package it.meridiana.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class DocLine extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id")
    @JsonIgnore
    private Doc document;

    private int lineNo;
    private String lineKind = "ITEM";
    private String itemId;
    private String code;
    private String description;
    private String unit = "pz";
    private BigDecimal qty = BigDecimal.ONE;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal discountPct = BigDecimal.ZERO;
    private BigDecimal vatRate = new BigDecimal("22");
    private BigDecimal taxable = BigDecimal.ZERO;
    private BigDecimal tax = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
}
