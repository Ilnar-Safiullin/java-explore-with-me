package ru.practicum.service;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequestDto;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationById(Long compId);

    CompilationDto addCompilation(NewCompilationDto request);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequestDto request);

    void deleteCompilation(Long compId);
}