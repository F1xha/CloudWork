package cl.duoc.diagnostico.service;
import cl.duoc.diagnostico.model.*;
import cl.duoc.diagnostico.repository.*;
import cl.duoc.diagnostico.dto.Contratos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
@Service @Transactional(readOnly=true)
public class DiagnosticoService {
 private final IncidenciaRepository incidencias; private final CategoriaRepository categorias;
 public DiagnosticoService(IncidenciaRepository i,CategoriaRepository c){incidencias=i;categorias=c;}
 public List<Categoria> catalogo(){return categorias.findAll();}
 public List<Vista> listar(){return incidencias.findAll(org.springframework.data.domain.Sort.by("fechaCreacion").descending()).stream().map(Vista::de).toList();}
 private Incidencia buscar(Long id){return incidencias.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Incidencia inexistente"));}
 private Categoria categoria(Long id){return categorias.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Categoría inexistente"));}
 public Vista obtener(Long id){return Vista.de(buscar(id));}
 @Transactional public Vista crear(Crear d){return Vista.de(incidencias.save(new Incidencia(d.titulo().trim(),d.descripcion().trim(),categoria(d.categoriaId()),d.prioridad())));}
 @Transactional public Vista editar(Long id,Editar d){var i=buscar(id);i.actualizar(d.titulo().trim(),d.descripcion().trim(),categoria(d.categoriaId()),d.prioridad(),d.estado());return Vista.de(i);}
 @Transactional public void eliminar(Long id){incidencias.delete(buscar(id));}
 @Transactional public Categoria crearCategoria(CategoriaNueva d){
  if(categorias.existsByNombreIgnoreCase(d.nombre().trim())) throw new ResponseStatusException(HttpStatus.CONFLICT,"La categoría ya existe");
  return categorias.save(new Categoria(d.nombre().trim()));
 }
 public Map<String,Long> reporte(){return Map.of("TOTAL",incidencias.count(),"ABIERTA",incidencias.countByEstado(Estado.ABIERTA),"EN_PROGRESO",incidencias.countByEstado(Estado.EN_PROGRESO),"RESUELTA",incidencias.countByEstado(Estado.RESUELTA));}
}
