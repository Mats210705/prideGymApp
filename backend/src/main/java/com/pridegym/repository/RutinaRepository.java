package com.pridegym.repository;

import com.pridegym.model.NivelRutina;
import com.pridegym.model.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RutinaRepository extends JpaRepository<Rutina, Long> {

    List<Rutina> findByActivaTrue();

    List<Rutina> findByAlumnoId(Long alumnoId);

    @Query("""
        SELECT r FROM Rutina r
        WHERE (:nivel IS NULL OR r.nivel = :nivel)
          AND (:disciplinaId IS NULL OR r.disciplina.id = :disciplinaId)
          AND (:alumnoId IS NULL OR r.alumno.id = :alumnoId)
          AND (:soloActivas = FALSE OR r.activa = TRUE)
          AND (:texto IS NULL OR :texto = ''
               OR LOWER(r.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(COALESCE(r.descripcion, '')) LIKE LOWER(CONCAT('%', :texto, '%')))
        ORDER BY r.fechaCreacion DESC, r.nombre ASC
    """)
    List<Rutina> findFiltered(@Param("nivel") NivelRutina nivel,
                              @Param("disciplinaId") Long disciplinaId,
                              @Param("alumnoId") Long alumnoId,
                              @Param("soloActivas") boolean soloActivas,
                              @Param("texto") String texto);
}
