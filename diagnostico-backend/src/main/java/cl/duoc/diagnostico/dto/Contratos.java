package cl.duoc.diagnostico.dto;
import cl.duoc.diagnostico.model.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
public final class Contratos {
 private Contratos(){}
 public record CategoriaNueva(@NotBlank @Size(max=100) String nombre){}
 public record Crear(@NotBlank @Size(max=150) String titulo,@NotBlank @Size(max=2000) String descripcion,
                     @NotNull @Positive Long categoriaId,@NotNull Prioridad prioridad){}
 public record Editar(@NotBlank @Size(max=150) String titulo,@NotBlank @Size(max=2000) String descripcion,
                     @NotNull @Positive Long categoriaId,@NotNull Prioridad prioridad,@NotNull Estado estado){}
 public record Vista(Long id,String titulo,String descripcion,Long categoriaId,String categoria,
                     Prioridad prioridad,Estado estado,Instant fechaCreacion){
  public static Vista de(Incidencia i){return new Vista(i.getId(),i.getTitulo(),i.getDescripcion(),
   i.getCategoria().getId(),i.getCategoria().getNombre(),i.getPrioridad(),i.getEstado(),i.getFechaCreacion());}
 }
}
