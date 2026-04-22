package com.pridegym.repository;

import com.pridegym.model.Cuota;
import com.pridegym.model.EstadoCuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface CuotaRepository extends JpaRepository<Cuota, Long> {

    List<Cuota> findByAlumnoIdOrderByAnioDescMesDesc(Long alumnoId);

    List<Cuota> findByEstado(EstadoCuota estado);

    List<Cuota> findByMesAndAnio(Integer mes, Integer anio);

    List<Cuota> findByAnioOrderByMesDesc(Integer anio);

    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM Cuota c WHERE c.estado = 'PAGADA' AND c.mes = :mes AND c.anio = :anio")
    BigDecimal sumIngresosDelMes(Integer mes, Integer anio);

    long countByEstadoAndMesAndAnio(EstadoCuota estado, Integer mes, Integer anio);

    long countByEstado(EstadoCuota estado);
}
