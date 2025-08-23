package ru.practicum.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompilationRequestDto {

    private Boolean pinned;

    @Size(min = 1, max = 50, message = "Название подборки должно быть от 1 до 50 символов")
    private String title;

    private List<Long> events;

}