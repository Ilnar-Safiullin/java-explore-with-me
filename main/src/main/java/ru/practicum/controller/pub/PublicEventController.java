package ru.practicum.controller.pub;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.filter.EventFilter;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class PublicEventController {
    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventFullDto getById(@PathVariable Long eventId, HttpServletRequest request) {
        return eventService.getEventById(eventId, request);
    }

    @GetMapping
    public List<EventShortDto> search(@SpringQueryMap EventFilter filter,
                                      HttpServletRequest request) {
        return eventService.searchPublic(filter, request);
    }
}