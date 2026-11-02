package it.meridiana.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "app_user")
public class AppUser extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String email;
    private String fullName;
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean active = true;
}
