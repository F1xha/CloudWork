package cl.duoc.ev2.api;
import cl.duoc.ev2.domain.Contracts.*;
import cl.duoc.ev2.application.IncidentService;
import cl.duoc.ev2.infrastructure.IncidentStore;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
@RestController @Profile("incidencias")
public class IncidentController {
 private final IncidentService service; private final IncidentStore store;
 public IncidentController(IncidentService service,IncidentStore store) { this.service=service;this.store=store; }
 @PostMapping("/api/incidencias") public ResponseEntity<Resultado> create(@Valid @RequestBody Crear request) {
  var result=service.create(request);
  return ResponseEntity.created(URI.create("/api/incidencias/"+result.id())).body(result);
 }
 @GetMapping("/api/incidencias") public List<Map<String,Object>> list() { return store.all(); }
 @GetMapping("/api/incidencias/{id}") public Map<String,Object> get(@PathVariable String id) {
  return store.all().stream().filter(x->id.equals(x.get("ID"))).findFirst().orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
 }
}
