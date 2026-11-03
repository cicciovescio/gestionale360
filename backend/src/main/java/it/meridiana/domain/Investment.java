package it.meridiana.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Investment extends BaseEntity {
    private String name;
    private String category;
    private BigDecimal amount = BigDecimal.ZERO;
    private LocalDate startDate = LocalDate.now();
    private int usefulLifeYears = 8;
    private BigDecimal expectedAnnualReturn = BigDecimal.ZERO;
    private BigDecimal salvageValue = BigDecimal.ZERO;
    @Column(columnDefinition = "text")
    private String notes;
}
