package cl.duoc.ev2;
import cl.duoc.ev2.application.IncidentService;
import cl.duoc.ev2.domain.Contracts.*;
import cl.duoc.ev2.infrastructure.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class IncidentServiceTest {
 private final CategoryClient categories=mock(CategoryClient.class);
 private final IncidentStore store=mock(IncidentStore.class);
 private final EventPublisher publisher=mock(EventPublisher.class);
 private final IncidentService service=new IncidentService(categories,store,publisher);
 private final Crear request=new Crear("Impresora","No imprime",1L,Prioridad.ALTA);
 @Test void invalidCategoryDoesNotSaveOrPublish() {
  when(categories.validate(1)).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));
  assertThrows(ResponseStatusException.class,()->service.create(request));
  verifyNoInteractions(store,publisher);
 }
 @Test void validationPrecedesPersistenceAndPublication() throws Exception {
  var result=service.create(request);
  var order=inOrder(categories,store,publisher);
  order.verify(categories).validate(1); order.verify(store).save(any(),eq("No imprime"));
  order.verify(publisher).publish(any()); order.verify(store).published(result.id());
  assertEquals("CONFIRMADA",result.publicacion());
 }
 @Test void brokerFailureIsVisibleAndDoesNotEraseSavedIncident() throws Exception {
  doThrow(new IllegalStateException("broker down")).when(publisher).publish(any());
  assertEquals("PENDIENTE",service.create(request).publicacion());
  verify(store).save(any(),any()); verify(store,never()).published(any());
 }
}
