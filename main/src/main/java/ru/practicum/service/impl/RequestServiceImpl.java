package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dao.EventRepository;
import ru.practicum.dao.RequestRepository;
import ru.practicum.dao.UserRepository;
import ru.practicum.dto.event.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.event.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.service.RequestService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getEventRequestsByUserId(Long userId, Long eventId) {
        log.info("getEventRequests({}, {})", userId, eventId);

        if (!eventRepository.existsEventByInitiatorId(userId)) {
            throw new BadRequestException("У юзера с id: " + userId + " нет ивента с id: " + eventId);
        }

        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional //незнаю насколько он тут нужен, просто вроде как лист сущностей приходит и записывается, вот повесил
    public EventRequestStatusUpdateResultDto changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequestDto dto) {
        log.info("Изменения статуса заявок инициатором на участие в ивенте ({}, {})", userId, eventId);
        Event event = eventRepository.findById(eventId)
                .filter(e -> e.getState().equals(EventState.PUBLISHED))
                .orElseThrow(() -> new NotFoundException("Request", "EventId", eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("У юзера с id: " + userId + " нет ивента с id: " + eventId);
        }

        if (requestRepository.isParticipantLimitReached(eventId, event.getParticipantLimit())) {
            throw new ConflictException("У Евента закончился лимит");
        }

        List<Request> requests = requestRepository.findAllById(dto.getRequestIds());
        requests.forEach(request -> {
            if (!request.getStatus().equals(EventRequestStatus.PENDING)) {
                throw new ConflictException("Не все запросы в статусе PENDING");
            }
        });

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            requests.forEach(r -> r.setStatus(EventRequestStatus.CONFIRMED));
            requestRepository.saveAll(requests);
            event.setConfirmedRequests(requests.size());
            eventRepository.save(event);
            List<ParticipationRequestDto> confirmedRequests = requests.stream()
                    .map(requestMapper::toDto)
                    .collect(Collectors.toList());
            log.debug("Смена статуса заявок на CONFIRMED завершена успешно. Лимит был равен 0 или отключена модерация");
            return new EventRequestStatusUpdateResultDto(confirmedRequests, new ArrayList<>());
        }

        if (dto.getStatus().equals(EventRequestStatus.REJECTED)) {
            requests.forEach(r -> r.setStatus(EventRequestStatus.REJECTED));
            requestRepository.saveAll(requests);
            List<ParticipationRequestDto> rejectedRequests = requests.stream()
                    .map(requestMapper::toDto)
                    .collect(Collectors.toList());
            log.debug("Смена статуса заявок на REJECTED завершена успешно.");
            return new EventRequestStatusUpdateResultDto(new ArrayList<>(), rejectedRequests);
        }

        log.info("Началась обработка смены статуса с учетом лимита");
        int confirmedCount = requestRepository.countConfirmedRequestsByEventId(eventId);
        int availableSlots = event.getParticipantLimit() - confirmedCount;

        List<Request> toConfirm = new ArrayList<>();
        List<Request> toReject = new ArrayList<>();

        for (Request request : requests) {
            if (availableSlots > 0) {
                request.setStatus(EventRequestStatus.CONFIRMED);
                toConfirm.add(request);
                availableSlots--;
            } else {
                request.setStatus(EventRequestStatus.REJECTED);
                toReject.add(request);
            }
        }

        requestRepository.saveAll(requests);
        if (!toConfirm.isEmpty()) {
            event.setConfirmedRequests(toConfirm.size());
            eventRepository.save(event);
        }
        List<ParticipationRequestDto> confirmedDtos = toConfirm.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());

        List<ParticipationRequestDto> rejectedDtos = toReject.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());

        log.debug("Смена статуса заявок завершена. Кол-во подтв: {}, кол-во отк: {}", confirmedDtos.size(), rejectedDtos.size());
        return new EventRequestStatusUpdateResultDto(confirmedDtos, rejectedDtos);
    }

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        log.info("Подача заявки юзером на евент ({}, {})", userId, eventId);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Request", "UserId", userId));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Request", "EventId", eventId));
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии.");
        }
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Ваш запрос уже отправлен");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Евент не опубликован");
        }
        if (requestRepository.isParticipantLimitReached(eventId, event.getParticipantLimit())) {
            throw new ConflictException("У Евента закончился лимит");
        }

        Request request = Request.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .build();

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(EventRequestStatus.CONFIRMED);
        } else {
            request.setStatus(EventRequestStatus.PENDING);
        }
        requestRepository.save(request);
        log.debug("Смена статуса заявки на {} прошла успешно", request.getStatus());
        return requestMapper.toDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getAllUserRequests(Long userId) {
        log.info("Получение All requests текущего пользователя в чужих евентах ");
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Request", "UserId", userId));
        List<ParticipationRequestDto> requestDtos = requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
        log.debug("Метод завершен кол-во request: {}", requestDtos.size());
        return requestDtos;
    }

    @Override
    public ParticipationRequestDto cancelUserRequest(Long userId, Long requestId) {
        log.info("Попытка отмены своего request пользователем ({}, {})", requestId, userId);
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Request", "RequestId", requestId));
        if (!request.getRequester().getId().equals(userId)) {
            throw new ConflictException("Пользователь может отменять только свои запросы");
        }
        request.setStatus(EventRequestStatus.CANCELED);
        requestRepository.save(request);
        log.info("Отмена произведена текущий статус {}", EventRequestStatus.CANCELED);
        return requestMapper.toDto(request);
    }
}