package it.meridiana.repo;

import it.meridiana.domain.BudgetEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetEntryRepository extends JpaRepository<BudgetEntry, String> {
    List<BudgetEntry> findByYearOrderByMonthAscCategoryAsc(int year);
}
