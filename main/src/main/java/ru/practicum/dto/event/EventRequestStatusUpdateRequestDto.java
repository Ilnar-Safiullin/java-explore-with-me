package ru.practicum.dto.event;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.model.EventRequestStatus;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequestDto {

    @NotEmpty (message = "Список не может быть пустым")
    private List<Long> requestIds;

    @NotNull (message = "Не указан статус")
    private EventRequestStatus status;
}