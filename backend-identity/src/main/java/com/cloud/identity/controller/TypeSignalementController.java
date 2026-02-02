package com.cloud.identity.controller;

import com.cloud.identity.entities.TypeSignalement;
import com.cloud.identity.repository.TypeSignalementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/types-signalement")
@CrossOrigin(origins = "*")
public class TypeSignalementController {

    @Autowired
    private TypeSignalementRepository typeSignalementRepository;

    @GetMapping
    public List<TypeSignalement> getAll() {
        return typeSignalementRepository.findAll();
    }

    @GetMapping("/{id}")
    public TypeSignalement getById(@PathVariable Integer id) {
        return typeSignalementRepository.findById(id).orElse(null);
    }

    @PostMapping
    public TypeSignalement create(@RequestBody TypeSignalement typeSignalement) {
        return typeSignalementRepository.save(typeSignalement);
    }

    @PutMapping("/{id}")
    public TypeSignalement update(@PathVariable Integer id, @RequestBody TypeSignalement typeSignalement) {
        typeSignalement.setId(id);
        return typeSignalementRepository.save(typeSignalement);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        typeSignalementRepository.deleteById(id);
    }
}
