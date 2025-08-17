package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequestDto;
import ru.practicum.model.EventState;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {

    private final EventService eventService;

    @PatchMapping("/{eventId}")
    public EventFullDto editEvent(@PathVariable Long eventId,
                                  @Valid @RequestBody UpdateEventAdminRequestDto dto) {
        return eventService.adminEditEvent(eventId, dto);
    }

    @GetMapping
    public List<EventFullDto> search(@RequestParam(required = false) List<Long> users,
                                     @RequestParam(required = false) List<EventState> states,
                                     @RequestParam(required = false) List<Long> categories,
                                     @RequestParam(required = false)
                                     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                     @RequestParam(required = false)
                                     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                     @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                     @RequestParam(defaultValue = "10") @Positive int size) {
        return eventService.searchAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }
}