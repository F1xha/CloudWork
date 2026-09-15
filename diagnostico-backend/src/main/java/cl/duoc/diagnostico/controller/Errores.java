package cl.duoc.diagnostico.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Map;
@RestControllerAdvice
public class Errores {
 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<?> validacion(MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("mensaje","Revisa los campos obligatorios y sus longitudes"));}
 @ExceptionHandler(DataIntegrityViolationException.class)
 public ResponseEntity<?> integridad(){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje","El registro ya existe o tiene relaciones que impiden la operación"));}
}
