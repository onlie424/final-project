package com.example.finalprojectb.service;

import com.example.finalprojectb.DTO.*;
import com.example.finalprojectb.model.Course;
import com.example.finalprojectb.model.Lesson;
import com.example.finalprojectb.model.Module;
import com.example.finalprojectb.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    public ModuleDTO createModule(CreateModuleDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + dto.getCourseId()));

        Module module = new Module();
        module.setCourse(course);
        module.setTitle(dto.getTitle());
        module.setDescription(dto.getDescription());
        module.setOrderIndex(dto.getOrderIndex());

        Module saved = moduleRepository.save(module);
        return convertToModuleDTO(saved);
    }

    @Transactional
    public ModuleDTO getModuleById(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found with id: " + moduleId));
        return convertToModuleDTOWithLessons(module);
    }

    public List<ModuleDTO> getModulesByCourse(Long courseId) {
        List<Module> modules =
                moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        return modules.stream()
                .map(this::convertToModuleDTOWithLessons)
                .collect(Collectors.toList());
    }

    @Transactional
    public ModuleDTO getModuleWithLessons(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found with id: " + moduleId));
        return convertToModuleDTOWithLessons(module);
    }

    public ModuleDTO updateModule(Long moduleId, CreateModuleDTO dto) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found with id: " + moduleId));

        module.setTitle(dto.getTitle());
        module.setDescription(dto.getDescription());
        module.setOrderIndex(dto.getOrderIndex());

        Module updated = moduleRepository.save(module);
        return convertToModuleDTO(updated);
    }

    public void deleteModule(Long moduleId) {
        moduleRepository.deleteById(moduleId);
    }

    private ModuleDTO convertToModuleDTO(Module module) {
        ModuleDTO dto = new ModuleDTO();
        dto.setId(module.getId());
        dto.setTitle(module.getTitle());
        dto.setDescription(module.getDescription());
        dto.setOrderIndex(module.getOrderIndex());
        return dto;
    }

    private ModuleDTO convertToModuleDTOWithLessons(Module module) {
        ModuleDTO dto = convertToModuleDTO(module);

        List<Lesson> lessons = lessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId());
        List<LessonDTO> lessonDTOs = lessons.stream()
                .map(this::convertToLessonDTO)
                .collect(Collectors.toList());

        dto.setLessons(lessonDTOs);
        return dto;
    }

    private LessonDTO convertToLessonDTO(Lesson lesson) {
        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setContentType(lesson.getContentType());
        dto.setContentUrl(lesson.getContentUrl());
        dto.setDurationMinutes(lesson.getDurationMinutes());
        dto.setOrderIndex(lesson.getOrderIndex());
        dto.setIsCompleted(false);
        return dto;
    }
}
