package cl.duoc.ev2.api;
import cl.duoc.ev2.domain.Contracts.Categoria;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
@RestController @Profile("categorias")
public class CategoryController {
 private final List<Categoria> categories=List.of(new Categoria(1,"Hardware"),new Categoria(2,"Software"),new Categoria(3,"Redes"));
 @GetMapping("/categorias") public List<Categoria> all() { return categories; }
 @GetMapping("/categorias/{id}") public Categoria get(@PathVariable long id) {
  return categories.stream().filter(c->c.id()==id).findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Categoría inexistente"));
 }
}
