package it.meridiana.repo;

import it.meridiana.domain.Installment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstallmentRepository extends JpaRepository<Installment, String> {
    List<Installment> findByReceivableOrderByDueDateAsc(boolean receivable);
    List<Installment> findByDocumentId(String documentId);
}
