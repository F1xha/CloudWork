package cl.duoc.diagnostico.repository;
import cl.duoc.diagnostico.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoriaRepository extends JpaRepository<Categoria,Long> { boolean existsByNombreIgnoreCase(String nombre); }
