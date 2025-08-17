package ru.practicum.service;

import ru.practicum.dto.event.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.event.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getAllUserRequests(Long userId);

    ParticipationRequestDto addRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelUserRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequestsByUserId(Long userId, Long eventId);

    EventRequestStatusUpdateResultDto changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequestDto updateRequestDto);

}