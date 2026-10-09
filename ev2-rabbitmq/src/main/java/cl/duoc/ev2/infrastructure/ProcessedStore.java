package cl.duoc.ev2.infrastructure;
import cl.duoc.ev2.domain.Contracts.IncidenciaCreada;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.*;
@Repository @Profile({"auditoria","notificaciones"})
public class ProcessedStore {
 private final JdbcTemplate db;
 public ProcessedStore(JdbcTemplate db) {
  this.db=db;
  db.execute("CREATE TABLE IF NOT EXISTS procesados (evento_id VARCHAR(36) PRIMARY KEY, incidencia_id VARCHAR(36), resultado VARCHAR(500), procesado_en VARCHAR(40))");
 }
 public void save(IncidenciaCreada e,String result) {
  db.update("MERGE INTO procesados KEY(evento_id) VALUES (?,?,?,?)",e.eventoId(),e.incidenciaId(),result,Instant.now().toString());
 }
 public List<Map<String,Object>> all() { return db.queryForList("SELECT * FROM procesados ORDER BY procesado_en"); }
}
