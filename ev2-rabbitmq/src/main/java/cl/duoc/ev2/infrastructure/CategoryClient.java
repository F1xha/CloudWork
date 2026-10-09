package cl.duoc.ev2.infrastructure;
import cl.duoc.ev2.domain.Contracts.Categoria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@Component @Profile("incidencias")
public class CategoryClient {
 private final RestClient http;
 public CategoryClient(@Value("${app.categories-url}") String url) {
  var factory=new SimpleClientHttpRequestFactory(); factory.setConnectTimeout(2000); factory.setReadTimeout(2000);
  http=RestClient.builder().baseUrl(url).requestFactory(factory).build();
 }
 public Categoria validate(long id) {
  try { return http.get().uri("/categorias/{id}",id).retrieve().body(Categoria.class); }
  catch (HttpClientErrorException.NotFound ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Categoría inexistente"); }
  catch (RestClientException ex) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Servicio de categorías no disponible"); }
 }
}
