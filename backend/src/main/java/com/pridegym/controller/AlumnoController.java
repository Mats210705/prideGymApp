package com.pridegym.controller;

import com.pridegym.dto.AlumnoRequest;
import com.pridegym.model.Alumno;
import com.pridegym.model.Cuota;
import com.pridegym.model.Disciplina;
import com.pridegym.repository.AlumnoRepository;
import com.pridegym.repository.CuotaRepository;
import com.pridegym.repository.DisciplinaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/alumnos")
public class AlumnoController {

    private final AlumnoRepository alumnoRepo;
    private final DisciplinaRepository disciplinaRepo;
    private final CuotaRepository cuotaRepo;

    public AlumnoController(AlumnoRepository alumnoRepo,
                            DisciplinaRepository disciplinaRepo,
                            CuotaRepository cuotaRepo) {
        this.alumnoRepo = alumnoRepo;
        this.disciplinaRepo = disciplinaRepo;
        this.cuotaRepo = cuotaRepo;
    }

    @GetMapping
    public List<Alumno> list() {
        return alumnoRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alumno> get(@PathVariable Long id) {
        return alumnoRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/cuotas")
    public ResponseEntity<List<Cuota>> cuotas(@PathVariable Long id) {
        if (!alumnoRepo.existsById(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(cuotaRepo.findByAlumnoIdOrderByAnioDescMesDesc(id));
    }

    @PostMapping
    public Alumno create(@RequestBody AlumnoRequest req) {
        Alumno a = new Alumno();
        apply(a, req);
        if (a.getFechaAlta() == null) a.setFechaAlta(LocalDate.now());
        return alumnoRepo.save(a);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Alumno> update(@PathVariable Long id, @RequestBody AlumnoRequest req) {
        return alumnoRepo.findById(id).map(a -> {
            apply(a, req);
            return ResponseEntity.ok(alumnoRepo.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!alumnoRepo.existsById(id)) return ResponseEntity.notFound().build();
        alumnoRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void apply(Alumno a, AlumnoRequest req) {
        if (req.getNombre() != null) a.setNombre(req.getNombre());
        if (req.getApellido() != null) a.setApellido(req.getApellido());
        if (req.getDni() != null) a.setDni(req.getDni());
        if (req.getEmail() != null) a.setEmail(req.getEmail());
        if (req.getTelefono() != null) a.setTelefono(req.getTelefono());
        if (req.getFechaNacimiento() != null) a.setFechaNacimiento(req.getFechaNacimiento());
        if (req.getFechaAlta() != null) a.setFechaAlta(req.getFechaAlta());
        if (req.getContactoEmergencia() != null) a.setContactoEmergencia(req.getContactoEmergencia());
        if (req.getActivo() != null) a.setActivo(req.getActivo());
        if (req.getDisciplinaIds() != null) {
            Set<Disciplina> set = new HashSet<>(disciplinaRepo.findAllById(req.getDisciplinaIds()));
            a.setDisciplinas(set);
        }
    }
}
