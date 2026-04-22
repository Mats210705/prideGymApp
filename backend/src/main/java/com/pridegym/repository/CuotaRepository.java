package com.pridegym.repository;

import com.pridegym.model.Cuota;
import com.pridegym.model.EstadoCuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CuotaRepository extends JpaRepository<Cuota, Long> {

    List<Cuota> findByAlumnoIdOrderByAnioDescMesDesc(Long alumnoId);

    List<Cuota> findByEstado(EstadoCuota estado);

    List<Cuota> findByMesAndAnio(Integer mes, Integer anio);

    List<Cuota> findByAnioOrderByMesDesc(Integer anio);

    @Query("""
        SELECT c FROM Cuota c
        WHERE (:mes IS NULL OR c.mes = :mes)
          AND (:anio IS NULL OR c.anio = :anio)
          AND (:estado IS NULL OR c.estado = :estado)
          AND (:texto IS NULL OR :texto = ''
               OR LOWER(c.alumno.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(c.alumno.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(c.alumno.dni) LIKE LOWER(CONCAT('%', :texto, '%')))
        ORDER BY c.anio DESC, c.mes DESC, c.alumno.apellido ASC, c.alumno.nombre ASC
    """)
    List<Cuota> findFiltered(@Param("mes") Integer mes,
                             @Param("anio") Integer anio,
                             @Param("estado") EstadoCuota estado,
                             @Param("texto") String texto);

    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM Cuota c WHERE c.estado = 'PAGADA' AND c.mes = :mes AND c.anio = :anio")
    BigDecimal sumIngresosDelMes(Integer mes, Integer anio);

    long countByEstadoAndMesAndAnio(EstadoCuota estado, Integer mes, Integer anio);

    long countByEstado(EstadoCuota estado);
}
