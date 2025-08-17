package ru.practicum.controller.pub;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{categoryId}")
    public CategoryDto getCategoryById(@PathVariable Long categoryId) {
        return categoryService.getCategoryById(categoryId);
    }

    @GetMapping
    public List<CategoryDto> getAllCategories(@RequestParam(defaultValue = "0") @Valid @PositiveOrZero Integer from,
                                              @RequestParam(defaultValue = "10") @Valid @Positive Integer size) {
        return categoryService.getAllCategories(from, size);
    }
}
