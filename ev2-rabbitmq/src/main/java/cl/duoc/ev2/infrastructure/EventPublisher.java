package cl.duoc.ev2.infrastructure;
import cl.duoc.ev2.domain.Contracts.IncidenciaCreada;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;
@Component @Profile("incidencias")
public class EventPublisher {
 private final RabbitTemplate rabbit;
 public EventPublisher(RabbitTemplate rabbit) { this.rabbit=rabbit; }
 public void publish(IncidenciaCreada event) throws Exception {
  var correlation=new CorrelationData(event.eventoId());
  rabbit.convertAndSend(MessagingConfig.EXCHANGE,MessagingConfig.ROUTING,event, message -> {
   message.getMessageProperties().setMessageId(event.eventoId());
   message.getMessageProperties().setDeliveryMode(org.springframework.amqp.core.MessageDeliveryMode.PERSISTENT);
   return message;
  },correlation);
  var confirm=correlation.getFuture().get(5,TimeUnit.SECONDS);
  if (!confirm.isAck() || correlation.getReturned()!=null)
   throw new IllegalStateException("Broker no confirmó el enrutamiento del evento");
 }
}
