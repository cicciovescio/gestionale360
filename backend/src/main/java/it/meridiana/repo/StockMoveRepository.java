package it.meridiana.repo;

import it.meridiana.domain.StockMove;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMoveRepository extends JpaRepository<StockMove, String> {
    List<StockMove> findAllByOrderByDateDescCreatedAtDesc();
    List<StockMove> findByDocumentId(String documentId);
}
