package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import ru.practicum.dto.event.filter.EventFilter;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequestDto;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {

    private final EventService eventService;
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @PatchMapping("/{eventId}")
    public EventFullDto editEvent(@PathVariable Long eventId,
                                  @Valid @RequestBody UpdateEventAdminRequestDto dto) {
        return eventService.adminEditEvent(eventId, dto);
    }

    @GetMapping
    public List<EventFullDto> search(@SpringQueryMap EventFilter eventFilter) {
        return eventService.searchAdmin(eventFilter);
    }
}