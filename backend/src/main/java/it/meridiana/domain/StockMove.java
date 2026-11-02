package it.meridiana.domain;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class StockMove extends BaseEntity {
    private LocalDate date;
    private String warehouseId;
    private String warehouseName;
    private String itemId;
    private String itemName;
    private BigDecimal qty;
    private String documentId;
    private String documentNumber;
    private String reason;
}
