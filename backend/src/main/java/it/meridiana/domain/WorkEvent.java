package it.meridiana.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class WorkEvent extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private WorkKind kind;
    private String title;
    private String customerName;
    private String technicianName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String status;
    @Lob
    private String notes;
}
