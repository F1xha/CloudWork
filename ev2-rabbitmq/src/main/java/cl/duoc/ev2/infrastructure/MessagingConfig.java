package cl.duoc.ev2.infrastructure;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.*;
@Configuration @Profile({"incidencias","auditoria","notificaciones"})
public class MessagingConfig {
 public static final String EXCHANGE="incidencias.exchange";
 public static final String ROUTING="incidencia.creada";
 public static final String AUDIT="incidencias.auditoria.queue";
 public static final String NOTIFY="incidencias.notificaciones.queue";
 @Bean Jackson2JsonMessageConverter jsonConverter() { return new Jackson2JsonMessageConverter(); }
 @Bean Declarables topology() {
  var exchange=new DirectExchange(EXCHANGE,true,false);
  var audit=new Queue(AUDIT,true); var notify=new Queue(NOTIFY,true);
  return new Declarables(exchange,audit,notify,
    BindingBuilder.bind(audit).to(exchange).with(ROUTING),
    BindingBuilder.bind(notify).to(exchange).with(ROUTING));
 }
}
