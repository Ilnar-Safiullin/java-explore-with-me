package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.RequestCategoryDto;
import ru.practicum.service.CategoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
@Validated
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody RequestCategoryDto requestCategoryDto) {
        return categoryService.addCategory(requestCategoryDto);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
    }

    @PatchMapping("/{categoryId}")
    public CategoryDto updateCategory(@PathVariable Long categoryId,
                                      @Valid @RequestBody RequestCategoryDto categoryDto) {
        return categoryService.updateCategory(categoryId, categoryDto);
    }
}