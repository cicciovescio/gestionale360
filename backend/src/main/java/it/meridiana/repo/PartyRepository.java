package it.meridiana.repo;

import it.meridiana.domain.Party;
import it.meridiana.domain.PartyKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyRepository extends JpaRepository<Party, String> {
    List<Party> findByKindOrderByNameAsc(PartyKind kind);
}
