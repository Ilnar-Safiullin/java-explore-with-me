package ru.practicum.dto.compilation;

import java.util.Set;
import lombok.*;
import ru.practicum.dto.event.EventShortDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    private Long id;
    private Boolean pinned;
    private String title;
    private Set<EventShortDto> events;
}