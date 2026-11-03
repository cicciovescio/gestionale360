package it.meridiana.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "bank_loan")
public class Loan extends BaseEntity {
    private String bank;
    private String purpose;
    private BigDecimal principal = BigDecimal.ZERO;
    private BigDecimal annualRatePct = BigDecimal.ZERO;
    private LocalDate startDate = LocalDate.now();
    private int durationMonths = 60;
    private BigDecimal installment = BigDecimal.ZERO;
    private BigDecimal paid = BigDecimal.ZERO;
    @Column(columnDefinition = "text")
    private String notes;
}
