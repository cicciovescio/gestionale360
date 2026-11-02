package it.meridiana.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class Party extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private PartyKind kind;
    private String legalForm;
    private String name;
    private String vatNumber;
    private String fiscalCode;
    private String sdiCode;
    private String pec;
    private String address;
    private String city;
    private String zip;
    private String province;
    private String email;
    private String phone;
    private String iban;
    @Enumerated(EnumType.STRING)
    private PaymentTerm paymentTerm = PaymentTerm.D30;
    private String priceListName = "Standard";
    private String agentName;
    private BigDecimal commissionPct = BigDecimal.ZERO;
    private boolean active = true;
    @Column(columnDefinition = "text")
    private String notes;
    @Column(length = 40)
    private String category;
}
