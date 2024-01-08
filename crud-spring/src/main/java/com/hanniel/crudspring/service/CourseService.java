package com.hanniel.crudspring.service;

import java.util.List;
import java.util.stream.Collectors;

import com.hanniel.crudspring.dto.CoursePageDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.hanniel.crudspring.dto.CourseDTO;
import com.hanniel.crudspring.dto.mapper.CourseMapper;
import com.hanniel.crudspring.exception.RecordNotFoundException;
import com.hanniel.crudspring.model.Course;
import com.hanniel.crudspring.repository.CourseRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Validated
@Service
public class CourseService {
   private final CourseRepository courseRepository;
     private final CourseMapper courseMapper;

    public CourseService(CourseRepository courseRepository, CourseMapper courseMapper){
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
    }

    public CoursePageDTO list(@PositiveOrZero int page,@Positive @Max(100) int pageSize){
        Page<Course> pageCourse = courseRepository.findAll(PageRequest.of(page, pageSize));
        List<CourseDTO> courses = pageCourse.get().map(courseMapper::toDTO).toList();
        return new CoursePageDTO(courses, pageCourse.getTotalElements(), pageCourse.getTotalPages());
    }

    public CourseDTO findById(@NotNull @Positive Long id){
      return courseRepository.findById(id).map(courseMapper::toDTO)
        .orElseThrow(() -> new RecordNotFoundException(id));
    } 

    public CourseDTO create(@Valid CourseDTO course){
      return courseMapper.toDTO(courseRepository.save(courseMapper.toEntity(course)));
    }

    public CourseDTO update(@NotNull @Positive Long id,  @Valid @NotNull CourseDTO courseDTO) {
      return courseRepository.findById(id) 
                .map(recordFound -> {
                  Course course = courseMapper.toEntity(courseDTO);
                  recordFound.setName(courseDTO.name());
                  recordFound.setCategory(this.courseMapper
                  .convertCategoryValue(courseDTO.category()));
                  recordFound.getLessons().clear();
                  course.getLessons().forEach(recordFound.getLessons()::add);
                  return courseMapper.toDTO(courseRepository.save(recordFound));
                }).orElseThrow(() -> new RecordNotFoundException(id));
      }

    public void delete(@NotNull @Positive Long id){
      courseRepository.delete(courseRepository.findById(id)
             .orElseThrow(() -> new RecordNotFoundException(id)));
    }
}
