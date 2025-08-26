package ru.practicum.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.client.StatsClient;
import ru.practicum.dao.CommentRepository;
import ru.practicum.dto.event.filter.EventFilter;
import ru.practicum.dto.event.*;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.model.*;
import ru.practicum.dao.CategoryRepository;
import ru.practicum.dao.EventRepository;
import ru.practicum.dao.UserRepository;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.Constants.DATE_TIME_FORMATTER;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;

    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final StatsClient statsClient;

    @Transactional
    @Override
    public EventFullDto add(Long userId, RequestEventDto requestEventDto) {
        log.info("Добавление евента юзером с id {}", userId);
        if (requestEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата не может быть раньше, чем через два часа от текущего момента");
        }
        User initiator = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", "id", userId));
        Category category = categoryRepository.findById(requestEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category", "id", requestEventDto.getCategory()));
        Location location = locationMapper.toEntity(requestEventDto.getLocation());
        Event event = eventMapper.toEvent(requestEventDto, initiator, category, location);
        eventRepository.save(event);
        log.debug("Добавлен евент с id {}", event.getId());
        return eventMapper.toFullDto(event);
    }

    @Override
    public EventFullDto getUserEventByUserIdAndEventId(Long userId, Long eventId) {
        log.info("Получение информации юзером с id: {} об евенте с id: {}", userId, eventId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", "id", userId));
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() -> new NotFoundException("Event", "id", eventId));
        log.debug("Получена информация о евенте с id: {}", eventId);
        return eventMapper.toFullDto(event);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("Получение юзером информации с id: {} о его созданных евентах", userId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", "id", userId));
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorIdOrderByEventDateDesc(pageable, userId);
        log.debug("Найдено {} событий для пользователя id={}", events.size(), userId);
        return events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventFullDto userUpdateEvent(Long userId, Long eventId, UpdateEventUserRequestDto dto) {
        log.info("Обновление юзером с id: {} евента с id {}", userId, eventId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", "id", userId));
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() -> new NotFoundException("Event", "id", eventId));

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Изменить можно только отмененные события или события в состоянии ожидания");
        }
        if (dto.getEventDate() != null && dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата не может быть раньше, чем через два часа от текущего момента");
        }
        eventMapper.updateUserEvent(dto, event);
        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category", "id", dto.getCategory()));
            event.setCategory(category);
        }
        if (dto.getLocation() != null) {
            event.setLocation(locationMapper.toEntity(dto.getLocation()));
        }
        if (dto.getStateAction() != null) {
            if (dto.getStateAction().equals(StateActionUser.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else {
                event.setState(EventState.CANCELED);
            }
        }
        Event updatedEvent = eventRepository.save(event);
        log.info("Евент с id: {} обновлен", eventId);
        return eventMapper.toFullDto(updatedEvent);
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        log.info("Паблик получение евента с id {}", eventId);
        Event event = eventRepository.findById(eventId)
                .filter(e -> e.getState().equals(EventState.PUBLISHED))
                .orElseThrow(() -> new NotFoundException("Event", "EventId", eventId));

        saveStats(request);
        EventFullDto eventFullDto = eventMapper.toFullDto(event);
        eventFullDto.setViews(getStats(event));
        Long comments = commentRepository.countByEventId(eventId);
        eventFullDto.setComments(comments);
        log.debug("Евент получен, статистика записана, евент с id {}", eventId);
        return eventFullDto;
    }

    private void saveStats(HttpServletRequest request) {
        try {
            EndpointHitDto hit = EndpointHitDto.builder()
                    .app("ewm-main-service")
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();
            statsClient.saveHit(hit);
        } catch (Exception e) {
            log.warn("Не удалось записать статистику: {}", e.getMessage());
        }
    }

    private Long getStats(Event event) {
        if (event.getPublishedOn() == null) {
            return 0L;
        }

        String start = event.getPublishedOn().format(DATE_TIME_FORMATTER);
        String end = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String uri = "/events/" + event.getId();

        try {
            List<ViewStatsDto> viewStatsDtos = statsClient.getStats(start, end, List.of(uri), true);
            if (viewStatsDtos == null || viewStatsDtos.isEmpty()) {
                return 0L;
            } else {
                return viewStatsDtos.get(0).getHits();
            }
        } catch (Exception e) {
            log.warn("Не удалось получить статистику: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public List<EventShortDto> searchPublic(EventFilter filter, HttpServletRequest request) {
        log.info("Начат паблик поиска евентов");
        checkRangeTime(filter.getRangeStart(), filter.getRangeEnd());

        Pageable pageable = PageRequest.of(filter.getFrom() / filter.getSize(), filter.getSize());
        Page<Event> events = eventRepository.findPublicEvents(
                filter.getText(),
                filter.getCategories(),
                filter.getPaid(),
                filter.getRangeStart(),
                filter.getRangeEnd(),
                filter.getOnlyAvailable(),
                pageable
        );

        if (events.isEmpty()) {
            return List.of();
        }
        saveStats(request);
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        Map<Long, Long> eventViewsMap = getViewsForEvents(events);
        Map<Long, Long> commentsByEventId = getCommentsByEventIds(eventIds);
        List<EventShortDto> result = events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    dto.setViews(eventViewsMap.getOrDefault(event.getId(), 0L));
                    dto.setComments(commentsByEventId.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());

        if ("EVENT_DATE".equals(filter.getSort())) {
            result.sort(Comparator.comparing(EventShortDto::getEventDate));
        } else if ("VIEWS".equals(filter.getSort())) {
            result.sort(Comparator.comparing(EventShortDto::getViews));
        }
        log.debug("Поиск завершен, размер списка {}", result.size());
        return result;
    }

    @Override
    @Transactional
    public EventFullDto adminEditEvent(Long eventId, UpdateEventAdminRequestDto dto) {
        log.info("Редактирование евента админом, евент id {}", eventId);
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event", "id", eventId));

        if (dto.getEventDate() != null && event.getPublishedOn() != null) {
            if (dto.getEventDate().isBefore(event.getPublishedOn().plusHours(1))) {
                throw new ConflictException("дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
        }
        eventMapper.updateAdminEvent(dto, event);
        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory()).orElseThrow(() -> new NotFoundException("Category", "id", dto.getCategory()));
            event.setCategory(category);
        }
        if (dto.getLocation() != null) {
            Location location = locationMapper.toEntity(dto.getLocation());
            event.setLocation(location);
        }

        if (dto.getStateAction() != null) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Евент можно публиковать и отменять, только если оно в состоянии ожидания публикации");
            }
            if (dto.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else {
                event.setState(EventState.CANCELED);
            }
        }
        event = eventRepository.save(event);
        log.debug("Редактирование админом завершено, текущий статус евента: {}", event.getState());
        return eventMapper.toFullDto(event);
    }

    @Override
    public List<EventFullDto> searchAdmin(EventFilter filter) {
        log.info("Начат админ поиск евентов");
        checkRangeTime(filter.getRangeStart(), filter.getRangeEnd());

        Pageable pageable = PageRequest.of(filter.getFrom() / filter.getSize(), filter.getSize());
        Page<Event> events = eventRepository.findAdminEvents(
                filter.getUsers(),
                filter.getStates(),
                filter.getCategories(),
                filter.getRangeStart(),
                filter.getRangeEnd(),
                pageable
        );
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        Map<Long, Long> eventViewsMap = getViewsForEvents(events);
        Map<Long, Long> commentsByEventId = getCommentsByEventIds(eventIds);
        List<EventFullDto> result = events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toFullDto(event);
                    dto.setViews(eventViewsMap.getOrDefault(event.getId(), 0L));
                    dto.setComments(commentsByEventId.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
        log.info("Поиск админом завершен кол-во элементов: {}", result.size());
        return result;
    }

    private void checkRangeTime(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("Начало должно быть до окончания");
        }
    }

    private Map<Long, Long> getViewsForEvents(Page<Event> events) {
        LocalDateTime earliestPublishedDate = events.stream()
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        if (eventIds.isEmpty() || earliestPublishedDate == null) {
            return new HashMap<>();
        }
        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());
        Map<String, Long> uriViewsMap = getViewsFromStats(uris, earliestPublishedDate);
        Map<Long, Long> eventViewsMap = new HashMap<>();
        for (Long eventId : eventIds) {
            String uri = "/events/" + eventId;
            eventViewsMap.put(eventId, uriViewsMap.getOrDefault(uri, 0L));
        }

        return eventViewsMap;
    }

    private Map<String, Long> getViewsFromStats(List<String> uris, LocalDateTime start) {
        try {
            LocalDateTime end = LocalDateTime.now();
            List<ViewStatsDto> stats = statsClient.getStats(
                    start.format(DATE_TIME_FORMATTER),
                    end.format(DATE_TIME_FORMATTER),
                    uris,
                    false
            );
            Map<String, Long> viewsMap = new HashMap<>();
            for (ViewStatsDto stat : stats) {
                viewsMap.put(stat.getUri(), stat.getHits());
            }
            return viewsMap;
        } catch (Exception e) {
            log.warn("Ошибка при получении данных из сервиса статистики: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private Map<Long, Long> getCommentsByEventIds(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> results = commentRepository.countCommentsByEventIdIn(eventIds);

        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1]
                ));
    }
}