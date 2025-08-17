package ru.practicum.controller.pub;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@Valid @PathVariable @Positive Long compId) {
        return compilationService.getCompilationById(compId);
    }

    @GetMapping
    public List<CompilationDto> getCompilations(
                                    @RequestParam(name = "pinned", required = false) Boolean pinned,
                                    @RequestParam(name = "from", defaultValue = "0") @Valid @PositiveOrZero Integer from,
                                    @RequestParam(name = "size", defaultValue = "10") @Valid @Positive Integer size) {
        return compilationService.getCompilations(pinned, from, size);
    }
}