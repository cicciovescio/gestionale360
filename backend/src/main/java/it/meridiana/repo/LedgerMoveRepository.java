package it.meridiana.repo;

import it.meridiana.domain.LedgerMove;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerMoveRepository extends JpaRepository<LedgerMove, String> {
    List<LedgerMove> findAllByOrderByDateDescCreatedAtDesc();
}
