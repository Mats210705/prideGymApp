package com.pridegym.controller;

import com.pridegym.dto.GenerarCuotasRequest;
import com.pridegym.dto.PagoRequest;
import com.pridegym.model.Alumno;
import com.pridegym.model.Cuota;
import com.pridegym.model.Disciplina;
import com.pridegym.model.EstadoCuota;
import com.pridegym.repository.AlumnoRepository;
import com.pridegym.repository.CuotaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cuotas")
public class CuotaController {

    private final CuotaRepository cuotaRepo;
    private final AlumnoRepository alumnoRepo;

    public CuotaController(CuotaRepository cuotaRepo, AlumnoRepository alumnoRepo) {
        this.cuotaRepo = cuotaRepo;
        this.alumnoRepo = alumnoRepo;
    }

    @GetMapping
    public List<Cuota> list(@RequestParam(required = false) Integer mes,
                            @RequestParam(required = false) Integer anio,
                            @RequestParam(required = false) EstadoCuota estado) {
        actualizarVencidas();
        if (mes != null && anio != null) return cuotaRepo.findByMesAndAnio(mes, anio);
        if (anio != null) return cuotaRepo.findByAnioOrderByMesDesc(anio);
        if (estado != null) return cuotaRepo.findByEstado(estado);
        return cuotaRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cuota> get(@PathVariable Long id) {
        return cuotaRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<Cuota> pagar(@PathVariable Long id, @RequestBody PagoRequest req) {
        return cuotaRepo.findById(id).map(c -> {
            c.setEstado(EstadoCuota.PAGADA);
            c.setFechaPago(req.getFechaPago() != null ? req.getFechaPago() : LocalDate.now());
            c.setMetodoPago(req.getMetodoPago());
            c.setObservaciones(req.getObservaciones());
            return ResponseEntity.ok(cuotaRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/revertir-pago")
    public ResponseEntity<Cuota> revertir(@PathVariable Long id) {
        return cuotaRepo.findById(id).map(c -> {
            c.setFechaPago(null);
            c.setMetodoPago(null);
            c.setEstado(c.getFechaVencimiento().isBefore(LocalDate.now())
                    ? EstadoCuota.VENCIDA : EstadoCuota.PENDIENTE);
            return ResponseEntity.ok(cuotaRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/generar-mes")
    public ResponseEntity<?> generarMes(@RequestBody GenerarCuotasRequest req) {
        if (req.getMes() == null || req.getAnio() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "mes y anio son requeridos"));
        }
        LocalDate venc = req.getFechaVencimiento() != null
                ? req.getFechaVencimiento()
                : LocalDate.of(req.getAnio(), req.getMes(), 10);

        List<Alumno> activos = alumnoRepo.findByActivoTrue();
        List<Cuota> existentes = cuotaRepo.findByMesAndAnio(req.getMes(), req.getAnio());
        List<Cuota> creadas = new ArrayList<>();

        for (Alumno a : activos) {
            boolean yaTiene = existentes.stream().anyMatch(c -> c.getAlumno().getId().equals(a.getId()));
            if (yaTiene) continue;
            BigDecimal monto = a.getDisciplinas().stream()
                    .map(Disciplina::getPrecioMensual)
                    .filter(p -> p != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (monto.compareTo(BigDecimal.ZERO) == 0) continue;

            Cuota c = new Cuota();
            c.setAlumno(a);
            c.setMes(req.getMes());
            c.setAnio(req.getAnio());
            c.setMonto(monto);
            c.setFechaVencimiento(venc);
            c.setEstado(EstadoCuota.PENDIENTE);
            creadas.add(cuotaRepo.save(c));
        }

        return ResponseEntity.ok(Map.of(
                "creadas", creadas.size(),
                "existentes", existentes.size(),
                "alumnosActivos", activos.size()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!cuotaRepo.existsById(id)) return ResponseEntity.notFound().build();
        cuotaRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void actualizarVencidas() {
        LocalDate hoy = LocalDate.now();
        List<Cuota> pendientes = cuotaRepo.findByEstado(EstadoCuota.PENDIENTE);
        for (Cuota c : pendientes) {
            if (c.getFechaVencimiento() != null && c.getFechaVencimiento().isBefore(hoy)) {
                c.setEstado(EstadoCuota.VENCIDA);
                cuotaRepo.save(c);
            }
        }
    }
}
