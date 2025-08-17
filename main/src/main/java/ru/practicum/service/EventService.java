package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.*;
import ru.practicum.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventFullDto add(Long userId, RequestEventDto dto);

    EventFullDto getUserEventByUserIdAndEventId(Long userId, Long eventId);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto userUpdateEvent(Long userId, Long eventId, UpdateEventUserRequestDto dto);

    List<EventFullDto> searchAdmin(List<Long> users,
                                   List<EventState> states,
                                   List<Long> categories,
                                   LocalDateTime rangeStart,
                                   LocalDateTime rangeEnd,
                                   int from,
                                   int size);

    EventFullDto adminEditEvent(Long eventId, UpdateEventAdminRequestDto dto);

    List<EventShortDto> searchPublic(String text,
                                     List<Long> categories,
                                     Boolean paid,
                                     LocalDateTime rangeStart,
                                     LocalDateTime rangeEnd,
                                     Boolean onlyAvailable,
                                     String sort,
                                     int from,
                                     int size,
                                     HttpServletRequest request);

    EventFullDto getEventById(Long eventId, HttpServletRequest request);
}
