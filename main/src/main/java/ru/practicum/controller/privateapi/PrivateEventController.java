package ru.practicum.controller.privateapi;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.EventService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import ru.practicum.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {
    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto add(@PathVariable Long userId,
                            @Valid @RequestBody RequestEventDto requestEventDto) {
        return eventService.add(userId, requestEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getUserEventByUserIdAndEventId(@PathVariable Long userId,
                                                       @PathVariable Long eventId) {
        return eventService.getUserEventByUserIdAndEventId(userId, eventId);
    }

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                             @RequestParam(defaultValue = "10") @Positive Integer size) {
        return eventService.getUserEvents(userId, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventUserRequestDto dto) {
        return eventService.userUpdateEvent(userId, eventId, dto);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequestsByUserId(@PathVariable Long userId,
                                                                  @PathVariable Long eventId) {
        return requestService.getEventRequestsByUserId(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResultDto changeRequestsStatus(@PathVariable Long userId,
                                                                  @PathVariable Long eventId,
                                                                  @RequestBody @Valid EventRequestStatusUpdateRequestDto updateRequest) {
        return requestService.changeRequestStatus(userId, eventId, updateRequest);
    }
}
