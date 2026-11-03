package it.meridiana.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class BudgetEntry extends BaseEntity {
    @Column(name = "year_no")
    private int year;
    @Column(name = "month_no")
    private int month;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 16)
    private BudgetKind kind = BudgetKind.REVENUE;
    private String category;
    private BigDecimal amount = BigDecimal.ZERO;
    @Column(columnDefinition = "text")
    private String notes;
}
