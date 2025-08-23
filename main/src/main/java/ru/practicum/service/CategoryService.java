package ru.practicum.service;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.RequestCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto addCategory(RequestCategoryDto requestCategoryDto);

    void deleteCategory(Long categoryId);

    CategoryDto updateCategory(Long categoryId, RequestCategoryDto categoryDto);

    CategoryDto getCategoryById(Long categoryId);

    List<CategoryDto> getAllCategories(Integer from, Integer size);

}