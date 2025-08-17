package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestEventDto {

    @NotBlank(message = "Поле annotation не может быть пустым")
    @Size(min = 20, max = 2000, message = "Поле annotation должно быть от 20 до 2000 символов")
    private String annotation;

    @NotNull(message = "Поле category не может быть пустым")
    private Long category;

    @NotBlank(message = "Поле description не может быть пустым")
    @Size(min = 20, max = 7000, message = "Поле description должно быть от 20 до 7000 символов")
    private String description;

    @NotNull(message = "Поле eventDate не может быть пустым")
    @Future(message = "Поле eventDate должно быть в будущем")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Поле location не может быть пустым")
    private LocationDto location;

    @Builder.Default
    private Boolean paid = false;

    @Builder.Default
    @PositiveOrZero(message = "Лимит участников должен быть положительным числом или нулём")
    private Long participantLimit = 0L;

    @Builder.Default
    private Boolean requestModeration = true;

    @NotBlank(message = "Поле title не может быть пустым")
    @Size(min = 3, max = 120, message = "Поле title должно быть от 3 до 120 символов")
    private String title;
}
