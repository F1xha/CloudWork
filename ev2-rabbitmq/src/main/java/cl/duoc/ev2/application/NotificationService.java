package cl.duoc.ev2.application;
import cl.duoc.ev2.domain.Contracts.IncidenciaCreada;
import cl.duoc.ev2.infrastructure.ProcessedStore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
@Service @Profile("notificaciones")
public class NotificationService {
 private final ProcessedStore store;
 public NotificationService(ProcessedStore store) { this.store=store; }
 public void process(IncidenciaCreada event) { store.save(event,"Notificación interna a soporte para incidencia "+event.incidenciaId()); }
}
