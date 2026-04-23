package com.pridegym.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Ejercicio {

    @Column(nullable = false, length = 200)
    private String nombre;

    private Integer series;

    @Column(length = 50)
    private String repeticiones; // "10-12", "30 seg", "al fallo"

    @Column(length = 50)
    private String descanso; // "60s", "1 min"

    @Column(length = 300)
    private String notas;

    private Integer orden;

    public Ejercicio() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getSeries() { return series; }
    public void setSeries(Integer series) { this.series = series; }

    public String getRepeticiones() { return repeticiones; }
    public void setRepeticiones(String repeticiones) { this.repeticiones = repeticiones; }

    public String getDescanso() { return descanso; }
    public void setDescanso(String descanso) { this.descanso = descanso; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
