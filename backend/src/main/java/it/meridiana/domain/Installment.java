package it.meridiana.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Installment extends BaseEntity {
    private String documentId;
    private String documentNumber;
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
    private String partyId;
    private String partyName;
    private boolean receivable;
    private LocalDate dueDate;
    private BigDecimal amount;
    private BigDecimal paid = BigDecimal.ZERO;
    private String method;
    private boolean reminderSent;
}
