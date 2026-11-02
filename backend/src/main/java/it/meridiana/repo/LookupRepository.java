package it.meridiana.repo;

import it.meridiana.domain.Lookup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LookupRepository extends JpaRepository<Lookup, String> {
    List<Lookup> findByGroupNameOrderByLabelAsc(String groupName);
}
