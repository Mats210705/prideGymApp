package com.pridegym.controller;

import com.pridegym.model.Alumno;
import com.pridegym.model.Disciplina;
import com.pridegym.model.NivelRutina;
import com.pridegym.model.Rutina;
import com.pridegym.repository.AlumnoRepository;
import com.pridegym.repository.DisciplinaRepository;
import com.pridegym.repository.RutinaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rutinas")
public class RutinaController {

    private final RutinaRepository rutinaRepo;
    private final AlumnoRepository alumnoRepo;
    private final DisciplinaRepository disciplinaRepo;

    public RutinaController(RutinaRepository rutinaRepo,
                            AlumnoRepository alumnoRepo,
                            DisciplinaRepository disciplinaRepo) {
        this.rutinaRepo = rutinaRepo;
        this.alumnoRepo = alumnoRepo;
        this.disciplinaRepo = disciplinaRepo;
    }

    @GetMapping
    public List<Rutina> list(@RequestParam(required = false) NivelRutina nivel,
                             @RequestParam(required = false) Long disciplinaId,
                             @RequestParam(required = false) Long alumnoId,
                             @RequestParam(required = false, defaultValue = "false") boolean soloActivas,
                             @RequestParam(required = false) String texto) {
        String textoNorm = (texto != null && !texto.isBlank()) ? texto.trim() : null;
        return rutinaRepo.findFiltered(nivel, disciplinaId, alumnoId, soloActivas, textoNorm);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rutina> get(@PathVariable Long id) {
        return rutinaRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Rutina create(@RequestBody Rutina rutina) {
        rutina.setId(null);
        resolverReferencias(rutina);
        return rutinaRepo.save(rutina);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rutina> update(@PathVariable Long id, @RequestBody Rutina body) {
        return rutinaRepo.findById(id).map(existing -> {
            existing.setNombre(body.getNombre());
            existing.setDescripcion(body.getDescripcion());
            existing.setNivel(body.getNivel() != null ? body.getNivel() : NivelRutina.PRINCIPIANTE);
            existing.setActiva(body.isActiva());
            existing.setEjercicios(body.getEjercicios());

            // Resolver FK (disciplina/alumno)
            if (body.getDisciplina() != null && body.getDisciplina().getId() != null) {
                existing.setDisciplina(disciplinaRepo.findById(body.getDisciplina().getId()).orElse(null));
            } else {
                existing.setDisciplina(null);
            }
            if (body.getAlumno() != null && body.getAlumno().getId() != null) {
                existing.setAlumno(alumnoRepo.findById(body.getAlumno().getId()).orElse(null));
            } else {
                existing.setAlumno(null);
            }

            return ResponseEntity.ok(rutinaRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!rutinaRepo.existsById(id)) return ResponseEntity.notFound().build();
        rutinaRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Duplica una rutina (util para crear variantes o asignar a otro alumno).
     */
    @PostMapping("/{id}/duplicar")
    public ResponseEntity<Rutina> duplicar(@PathVariable Long id) {
        return rutinaRepo.findById(id).map(src -> {
            Rutina copia = new Rutina();
            copia.setNombre(src.getNombre() + " (copia)");
            copia.setDescripcion(src.getDescripcion());
            copia.setNivel(src.getNivel());
            copia.setDisciplina(src.getDisciplina());
            copia.setAlumno(src.getAlumno());
            copia.setEjercicios(new java.util.ArrayList<>(src.getEjercicios()));
            copia.setActiva(true);
            return ResponseEntity.ok(rutinaRepo.save(copia));
        }).orElse(ResponseEntity.notFound().build());
    }

    private void resolverReferencias(Rutina r) {
        // Si vienen con solo id (desde el frontend), traer entidad completa para no romper FK
        Disciplina d = r.getDisciplina();
        if (d != null && d.getId() != null) {
            r.setDisciplina(disciplinaRepo.findById(d.getId()).orElse(null));
        } else {
            r.setDisciplina(null);
        }
        Alumno a = r.getAlumno();
        if (a != null && a.getId() != null) {
            r.setAlumno(alumnoRepo.findById(a.getId()).orElse(null));
        } else {
            r.setAlumno(null);
        }
    }
}
