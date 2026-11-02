package it.meridiana.domain;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Lookup extends BaseEntity {
    private String groupName;
    private String code;
    private String label;
    private boolean active = true;
}
