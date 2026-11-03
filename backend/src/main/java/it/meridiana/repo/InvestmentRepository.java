package it.meridiana.repo;

import it.meridiana.domain.Investment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestmentRepository extends JpaRepository<Investment, String> {
    List<Investment> findAllByOrderByStartDateDesc();
}
