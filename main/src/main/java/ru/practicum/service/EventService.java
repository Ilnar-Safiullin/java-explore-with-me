package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.filter.EventFilter;
import ru.practicum.dto.event.*;

import java.util.List;

public interface EventService {

    EventFullDto add(Long userId, RequestEventDto dto);

    EventFullDto getUserEventByUserIdAndEventId(Long userId, Long eventId);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto userUpdateEvent(Long userId, Long eventId, UpdateEventUserRequestDto dto);

    List<EventFullDto> searchAdmin(EventFilter filter);

    EventFullDto adminEditEvent(Long eventId, UpdateEventAdminRequestDto dto);

    List<EventShortDto> searchPublic(EventFilter filter, HttpServletRequest request);

    EventFullDto getEventById(Long eventId, HttpServletRequest request);
}
