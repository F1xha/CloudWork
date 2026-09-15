package cl.duoc.diagnostico.model;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="incidencias")
public class Incidencia {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=150) private String titulo;
 @Column(nullable=false,length=2000) private String descripcion;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="categoria_id",nullable=false) private Categoria categoria;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Prioridad prioridad;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Estado estado;
 @Column(nullable=false,updatable=false) private Instant fechaCreacion;
 protected Incidencia() {}
 public Incidencia(String titulo,String descripcion,Categoria categoria,Prioridad prioridad){
  this.titulo=titulo;this.descripcion=descripcion;this.categoria=categoria;this.prioridad=prioridad;
  this.estado=Estado.ABIERTA;this.fechaCreacion=Instant.now();
 }
 public void actualizar(String titulo,String descripcion,Categoria categoria,Prioridad prioridad,Estado estado){
  this.titulo=titulo;this.descripcion=descripcion;this.categoria=categoria;this.prioridad=prioridad;this.estado=estado;
 }
 public Long getId(){return id;} public String getTitulo(){return titulo;}
 public String getDescripcion(){return descripcion;} public Categoria getCategoria(){return categoria;}
 public Prioridad getPrioridad(){return prioridad;} public Estado getEstado(){return estado;}
 public Instant getFechaCreacion(){return fechaCreacion;}
}
