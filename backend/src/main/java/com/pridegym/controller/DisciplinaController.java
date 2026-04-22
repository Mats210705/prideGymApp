package com.pridegym.controller;

import com.pridegym.model.Disciplina;
import com.pridegym.repository.DisciplinaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disciplinas")
public class DisciplinaController {

    private final DisciplinaRepository repo;

    public DisciplinaController(DisciplinaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Disciplina> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Disciplina> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Disciplina create(@RequestBody Disciplina d) {
        d.setId(null);
        return repo.save(d);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Disciplina> update(@PathVariable Long id, @RequestBody Disciplina body) {
        return repo.findById(id).map(d -> {
            d.setNombre(body.getNombre());
            d.setPrecioMensual(body.getPrecioMensual());
            d.setDescripcion(body.getDescripcion());
            d.setActiva(body.isActiva());
            return ResponseEntity.ok(repo.save(d));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
