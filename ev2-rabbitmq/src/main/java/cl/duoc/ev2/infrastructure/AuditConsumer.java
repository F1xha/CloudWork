package cl.duoc.ev2.infrastructure;
import cl.duoc.ev2.domain.Contracts.IncidenciaCreada;
import cl.duoc.ev2.application.AuditService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.slf4j.*;
@Component @Profile("auditoria")
public class AuditConsumer {
 private final AuditService service;
 private static final Logger log=LoggerFactory.getLogger(AuditConsumer.class);
 public AuditConsumer(AuditService service) { this.service=service; }
 @RabbitListener(queues=MessagingConfig.AUDIT)
 public void receive(IncidenciaCreada event) {
  service.process(event);
  log.info("CONSUMED consumer=auditoria eventoId={} incidenciaId={}",event.eventoId(),event.incidenciaId());
 }
}
