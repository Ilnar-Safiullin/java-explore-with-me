package ru.practicum.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {

    @Builder.Default
    private Boolean pinned = false;

    @NotBlank(message = "Title не может быть пустым")
    @Size(max = 50, message = "Title не более 50 символов")
    private String title;

    private List<Long> events;
}