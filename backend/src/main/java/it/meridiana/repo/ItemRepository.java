package it.meridiana.repo;

import it.meridiana.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, String> {
    List<Item> findByActiveTrueOrderByNameAsc();
}
