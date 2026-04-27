package com.example.finalprojectb.controller;

import com.example.finalprojectb.DTO.CreateModuleDTO;
import com.example.finalprojectb.DTO.LessonDTO;
import com.example.finalprojectb.DTO.ModuleDTO;
import com.example.finalprojectb.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    // Create new module
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleDTO> createModule(@RequestBody CreateModuleDTO dto) {
        ModuleDTO created = moduleService.createModule(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Get module by ID
    @GetMapping("/{id}")
    public ResponseEntity<ModuleDTO> getModuleById(@PathVariable Long id) {
        ModuleDTO module = moduleService.getModuleWithLessons(id);
        return ResponseEntity.ok(module);
    }

    // Get lessons for a module
    @GetMapping("/{id}/lessons")
    public ResponseEntity<List<LessonDTO>> getModuleLessons(@PathVariable Long id) {
        ModuleDTO module = moduleService.getModuleWithLessons(id);
        return ResponseEntity.ok(module.getLessons());
    }

    // Get all modules for a course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ModuleDTO>> getModulesByCourse(@PathVariable Long courseId) {
        List<ModuleDTO> modules = moduleService.getModulesByCourse(courseId);
        return ResponseEntity.ok(modules);
    }

    // Update module
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleDTO> updateModule(
            @PathVariable Long id,
            @RequestBody CreateModuleDTO dto) {
        ModuleDTO updated = moduleService.updateModule(id, dto);
        return ResponseEntity.ok(updated);
    }

    // Delete module
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        moduleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }

    // Exception handler
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}