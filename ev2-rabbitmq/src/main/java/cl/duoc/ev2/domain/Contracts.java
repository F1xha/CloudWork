package cl.duoc.ev2.domain;
import jakarta.validation.constraints.*;
import java.time.Instant;
public final class Contracts {
 private Contracts() {}
 public enum Prioridad { BAJA, MEDIA, ALTA }
 public record Crear(@NotBlank @Size(max=150) String titulo,
   @NotBlank @Size(max=2000) String descripcion, @NotNull @Positive Long categoriaId,
   @NotNull Prioridad prioridad) {}
 public record Categoria(long id, String nombre) {}
 public record IncidenciaCreada(String eventoId, String incidenciaId, String titulo,
   long categoriaId, Prioridad prioridad, Instant fechaCreacion) {}
 public record Resultado(String id, String estado, String eventoId, String publicacion) {}
}
