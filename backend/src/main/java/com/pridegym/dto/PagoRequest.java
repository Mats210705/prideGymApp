package com.pridegym.dto;

import com.pridegym.model.MetodoPago;
import java.time.LocalDate;

public class PagoRequest {
    private LocalDate fechaPago;
    private MetodoPago metodoPago;
    private String observaciones;

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
