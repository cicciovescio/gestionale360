package it.meridiana.domain;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Company extends BaseEntity {
    private String name;
    private String vatNumber;
    private String fiscalCode;
    private String pec;
    private String sdiCode;
    private String address;
    private String city;
    private String zip;
    private String province;
    private String phone;
    private String email;
    private String iban;
    private String bankName;
    @jakarta.persistence.Column(columnDefinition = "text")
    private String notes;
    private int einvoiceCredits = 1500;
}
