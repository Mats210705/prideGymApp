package com.pridegym.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rutinas")
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelRutina nivel = NivelRutina.PRINCIPIANTE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "disciplina_id")
    private Disciplina disciplina;

    // Rutina puede ser generica (alumno null) o asignada a un alumno
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "alumno_id")
    @JsonIgnoreProperties({"cuotas", "disciplinas"})
    private Alumno alumno;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rutina_ejercicios", joinColumns = @JoinColumn(name = "rutina_id"))
    @OrderColumn(name = "orden_idx")
    private List<Ejercicio> ejercicios = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate fechaCreacion = LocalDate.now();

    private boolean activa = true;

    public Rutina() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public NivelRutina getNivel() { return nivel; }
    public void setNivel(NivelRutina nivel) { this.nivel = nivel; }

    public Disciplina getDisciplina() { return disciplina; }
    public void setDisciplina(Disciplina disciplina) { this.disciplina = disciplina; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public List<Ejercicio> getEjercicios() { return ejercicios; }
    public void setEjercicios(List<Ejercicio> ejercicios) { this.ejercicios = ejercicios; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
