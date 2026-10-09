package cl.duoc.ev2.api;
import cl.duoc.ev2.infrastructure.ProcessedStore;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @Profile({"auditoria","notificaciones"})
public class ProcessedController {
 private final ProcessedStore store;
 public ProcessedController(ProcessedStore store) { this.store=store; }
 @GetMapping("/procesados") public List<Map<String,Object>> all() { return store.all(); }
}
