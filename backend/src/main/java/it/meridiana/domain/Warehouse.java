package it.meridiana.domain;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Warehouse extends BaseEntity {
    private String name;
    private String address;
    private boolean main;
}
