package ru.practicum.dto.event.filter;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventFilter {
    private List<Long> categories;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    @PositiveOrZero
    private Integer from = 0;

    @Positive
    private Integer size = 10;

    private String text;
    private Boolean paid;
    private Boolean onlyAvailable = false;
    private String sort;

    private List<Long> users;
    private List<EventState> states;

    public boolean isPublicSearch() {
        return text != null || paid != null || sort != null || onlyAvailable != null;
    }

    public boolean isAdminSearch() {
        return users != null || states != null;
    }
}