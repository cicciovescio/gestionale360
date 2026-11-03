package it.meridiana.repo;

import it.meridiana.domain.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, String> {
    List<Loan> findAllByOrderByStartDateDesc();
}
