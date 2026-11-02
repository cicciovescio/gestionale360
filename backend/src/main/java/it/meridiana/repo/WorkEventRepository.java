package it.meridiana.repo;

import it.meridiana.domain.WorkEvent;
import it.meridiana.domain.WorkKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkEventRepository extends JpaRepository<WorkEvent, String> {
    List<WorkEvent> findByKindOrderByStartAtDesc(WorkKind kind);
    List<WorkEvent> findAllByOrderByStartAtAsc();
}
