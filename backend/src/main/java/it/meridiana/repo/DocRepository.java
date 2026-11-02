package it.meridiana.repo;

import it.meridiana.domain.Doc;
import it.meridiana.domain.DocumentStatus;
import it.meridiana.domain.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface DocRepository extends JpaRepository<Doc, String> {
    List<Doc> findByTypeOrderByDateDescNumberDesc(DocumentType type);

    long countByTypeAndSeriesAndDateBetween(DocumentType type, String series, LocalDate from, LocalDate to);

    List<Doc> findByTypeInAndDateBetween(List<DocumentType> types, LocalDate from, LocalDate to);

    List<Doc> findByStatusIn(List<DocumentStatus> statuses);

    @Query("select coalesce(max(d.number), '000000') from Doc d where d.type = :type and d.series = :series")
    String maxNumber(DocumentType type, String series);
}
