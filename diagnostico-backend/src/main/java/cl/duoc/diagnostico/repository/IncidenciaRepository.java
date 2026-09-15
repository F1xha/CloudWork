package cl.duoc.diagnostico.repository;
import cl.duoc.diagnostico.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface IncidenciaRepository extends JpaRepository<Incidencia,Long> { long countByEstado(Estado estado); }
