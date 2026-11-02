package it.meridiana.domain;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class LedgerMove extends BaseEntity {
    private LocalDate date;
    private String account;
    private String counterpart;
    private String description;
    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal expense = BigDecimal.ZERO;
    private String documentId;
}
