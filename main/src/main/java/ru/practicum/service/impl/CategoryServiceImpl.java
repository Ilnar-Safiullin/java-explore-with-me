package ru.practicum.service.impl;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dao.CategoryRepository;
import ru.practicum.dao.EventRepository;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.RequestCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.model.Category;
import ru.practicum.service.CategoryService;

import java.util.List;

@Transactional (readOnly = true)
@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CategoryDto addCategory(RequestCategoryDto requestCategoryDto) {
        log.info("Добавление категории: {}", requestCategoryDto);
        if (categoryRepository.existsByNameIgnoreCase(requestCategoryDto.getName())) {
            throw new AlreadyExistsException("Категория", "name", requestCategoryDto.getName());
        }
        Category category = categoryMapper.toEntity(requestCategoryDto);
        categoryRepository.save(category);
        log.debug("Категория добавлена: {}", category);
        return categoryMapper.toDto(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("Удаление категории с ID {}", categoryId);
        categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Category", "Id", categoryId));
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("Нельзя удалять связанные сущности");
        }
        categoryRepository.deleteById(categoryId);
        log.debug("Удалена категория с ID {}", categoryId);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(Long categoryId, RequestCategoryDto requestCategoryDto) {
        log.info("Обновление категории с ID {}", categoryId);
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Category", "Id", categoryId));
        category.setName(requestCategoryDto.getName());
        category = categoryRepository.save(category);
        log.debug("Категория с ID {} обновлена, новое имя {}", categoryId, category.getName());
        return categoryMapper.toDto(category);
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        log.info("Получение категории с ID: {}", categoryId);
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Category", "Id", categoryId));
        log.debug("Получена категория с ID: {}", categoryId);
        return categoryMapper.toDto(category);
    }

    @Override
    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        log.info("Получение категорий from={}, size={}", from, size);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        List<CategoryDto> categories = categoryRepository.findAll(pageable)
                .map(categoryMapper::toDto)
                .getContent();

        log.info("Успешно получено {} категорий", categories.size());
        return categories;
    }
}