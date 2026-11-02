package it.meridiana.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class Item extends BaseEntity {
    @Column(unique = true)
    private String code;
    private String name;
    private String kind;
    private String category;
    private String brand;
    private String unit = "pz";
    private BigDecimal vatRate = new BigDecimal("22");
    private BigDecimal listPrice = BigDecimal.ZERO;
    private BigDecimal costPrice = BigDecimal.ZERO;
    private boolean stockManaged = true;
    private BigDecimal stockQty = BigDecimal.ZERO;
    private String warehouseName = "Sede";
    @Column(columnDefinition = "text")
    private String notes;
    private boolean active = true;
}
