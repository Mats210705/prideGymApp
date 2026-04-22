package com.pridegym.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "disciplinas")
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioMensual;

    @Column(length = 500)
    private String descripcion;

    private boolean activa = true;

    public Disciplina() {}

    public Disciplina(String nombre, BigDecimal precioMensual, String descripcion) {
        this.nombre = nombre;
        this.precioMensual = precioMensual;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecioMensual() { return precioMensual; }
    public void setPrecioMensual(BigDecimal precioMensual) { this.precioMensual = precioMensual; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
