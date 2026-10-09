package cl.duoc.ev2.application;
import cl.duoc.ev2.domain.Contracts.*;
import cl.duoc.ev2.infrastructure.*;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.slf4j.*;
import java.time.Instant;
import java.util.UUID;
@Service @Profile("incidencias")
public class IncidentService {
 private static final Logger log=LoggerFactory.getLogger(IncidentService.class);
 private final CategoryClient categories; private final IncidentStore store; private final EventPublisher publisher;
 public IncidentService(CategoryClient categories,IncidentStore store,EventPublisher publisher) {
  this.categories=categories;this.store=store;this.publisher=publisher;
 }
 public Resultado create(Crear request) {
  categories.validate(request.categoriaId());
  log.info("SYNC categoria={} validada por HTTP",request.categoriaId());
  var event=new IncidenciaCreada(UUID.randomUUID().toString(),UUID.randomUUID().toString(),request.titulo().trim(),request.categoriaId(),request.prioridad(),Instant.now());
  store.save(event,request.descripcion().trim());
  try {
   publisher.publish(event); store.published(event.incidenciaId());
   log.info("PUBLISHED eventoId={} incidenciaId={} exchange={} routing={}",event.eventoId(),event.incidenciaId(),MessagingConfig.EXCHANGE,MessagingConfig.ROUTING);
   return new Resultado(event.incidenciaId(),"ABIERTA",event.eventoId(),"CONFIRMADA");
  } catch (Exception ex) {
   if(ex instanceof InterruptedException) Thread.currentThread().interrupt();
   log.error("PUBLISH_FAILED eventoId={} incidenciaId={} guardada; entrega no confirmada",event.eventoId(),event.incidenciaId(),ex);
   return new Resultado(event.incidenciaId(),"ABIERTA",event.eventoId(),"PENDIENTE");
  }
 }
}
