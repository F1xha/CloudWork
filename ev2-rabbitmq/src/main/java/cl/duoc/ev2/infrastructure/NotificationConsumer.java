package cl.duoc.ev2.infrastructure;
import cl.duoc.ev2.domain.Contracts.IncidenciaCreada;
import cl.duoc.ev2.application.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.slf4j.*;
@Component @Profile("notificaciones")
public class NotificationConsumer {
 private final NotificationService service;
 private static final Logger log=LoggerFactory.getLogger(NotificationConsumer.class);
 public NotificationConsumer(NotificationService service) { this.service=service; }
 @RabbitListener(queues=MessagingConfig.NOTIFY)
 public void receive(IncidenciaCreada event) {
  service.process(event);
  log.info("CONSUMED consumer=notificaciones eventoId={} incidenciaId={}",event.eventoId(),event.incidenciaId());
 }
}
