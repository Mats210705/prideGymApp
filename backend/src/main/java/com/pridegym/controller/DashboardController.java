package com.pridegym.controller;

import com.pridegym.model.EstadoCuota;
import com.pridegym.repository.AlumnoRepository;
import com.pridegym.repository.CuotaRepository;
import com.pridegym.repository.DisciplinaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AlumnoRepository alumnoRepo;
    private final DisciplinaRepository disciplinaRepo;
    private final CuotaRepository cuotaRepo;

    public DashboardController(AlumnoRepository alumnoRepo,
                               DisciplinaRepository disciplinaRepo,
                               CuotaRepository cuotaRepo) {
        this.alumnoRepo = alumnoRepo;
        this.disciplinaRepo = disciplinaRepo;
        this.cuotaRepo = cuotaRepo;
    }

    @GetMapping
    public Map<String, Object> resumen() {
        LocalDate hoy = LocalDate.now();
        int mes = hoy.getMonthValue();
        int anio = hoy.getYear();

        Map<String, Object> r = new HashMap<>();
        r.put("alumnosActivos", alumnoRepo.countByActivoTrue());
        r.put("alumnosTotales", alumnoRepo.count());
        r.put("disciplinas", disciplinaRepo.count());

        BigDecimal ingresosMes = cuotaRepo.sumIngresosDelMes(mes, anio);
        r.put("ingresosDelMes", ingresosMes != null ? ingresosMes : BigDecimal.ZERO);

        r.put("cuotasPendientesMes", cuotaRepo.countByEstadoAndMesAndAnio(EstadoCuota.PENDIENTE, mes, anio));
        r.put("cuotasPagadasMes", cuotaRepo.countByEstadoAndMesAndAnio(EstadoCuota.PAGADA, mes, anio));
        r.put("cuotasVencidasTotal", cuotaRepo.countByEstado(EstadoCuota.VENCIDA));

        r.put("mes", mes);
        r.put("anio", anio);

        return r;
    }
}
