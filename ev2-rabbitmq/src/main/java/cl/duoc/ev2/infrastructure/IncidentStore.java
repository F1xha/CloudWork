package cl.duoc.ev2.infrastructure;
import cl.duoc.ev2.domain.Contracts.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;
import java.util.*;
@Repository @Profile("incidencias")
public class IncidentStore {
 private final JdbcTemplate db;
 public IncidentStore(JdbcTemplate db) {
  this.db=db;
  db.execute("CREATE TABLE IF NOT EXISTS incidencias (id VARCHAR(36) PRIMARY KEY, evento_id VARCHAR(36), titulo VARCHAR(150), descripcion VARCHAR(2000), categoria_id BIGINT, prioridad VARCHAR(10), fecha VARCHAR(40), publicacion VARCHAR(20))");
 }
 public void save(IncidenciaCreada e, String descripcion) {
  db.update("INSERT INTO incidencias VALUES (?,?,?,?,?,?,?,?)", e.incidenciaId(),e.eventoId(),e.titulo(),descripcion,e.categoriaId(),e.prioridad().name(),e.fechaCreacion().toString(),"PENDIENTE");
 }
 public void published(String id) { db.update("UPDATE incidencias SET publicacion='CONFIRMADA' WHERE id=?",id); }
 public List<Map<String,Object>> all() { return db.queryForList("SELECT * FROM incidencias ORDER BY fecha"); }
}
