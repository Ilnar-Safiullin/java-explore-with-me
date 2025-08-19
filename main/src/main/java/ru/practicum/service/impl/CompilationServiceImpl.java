package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ViewStatsDto;
import ru.practicum.client.StatsClient;
import ru.practicum.dao.EventRepository;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequestDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.dao.CompilationRepository;
import ru.practicum.service.CompilationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional (readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final StatsClient statsClient;


    @Transactional
    @Override
    public CompilationDto addCompilation(NewCompilationDto newComDto) {
        log.info("Добавление compilation");
        if (compilationRepository.existsCompilationByTitle(newComDto.getTitle())) {
            throw new AlreadyExistsException("Compilation", "title", newComDto.getTitle());
        }
        Compilation compilation = compilationMapper.toEntity(newComDto);
        HashSet<Event> events = new HashSet<>();
        if (newComDto.getEvents() != null && !newComDto.getEvents().isEmpty()) {
            List<Event> event = eventRepository.findAllById(newComDto.getEvents());
            events.addAll(event);
        }
        compilation.setEvents(events);
        compilation = compilationRepository.save(compilation);
        log.debug("Подборка успешно создана");
        CompilationDto result = compilationMapper.toDto(compilation);
        return addStats(result);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequestDto updateComReqDto) {
        log.info("Обновление подборки с id={} с данными: {}", compId, updateComReqDto);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation", "Id", compId));
        if (updateComReqDto.getTitle() != null && !updateComReqDto.getTitle().isBlank() &&
            compilationRepository.existsCompilationByTitle(updateComReqDto.getTitle()) &&
            !compilation.getTitle().equalsIgnoreCase(updateComReqDto.getTitle())) {
            throw new AlreadyExistsException("Compilation", "title", updateComReqDto.getTitle());
        }
        if (updateComReqDto.getTitle() != null && !updateComReqDto.getTitle().isBlank()) {
            compilation.setTitle(updateComReqDto.getTitle());
        }
        if (updateComReqDto.getPinned() != null) {
            compilation.setPinned(updateComReqDto.getPinned());
        }
        if (updateComReqDto.getEvents() != null) {
            compilation.getEvents().clear();
            Set<Event> events;
            if (updateComReqDto.getEvents().isEmpty()) {
                events = new HashSet<>();
            } else {
                events = loadEvents(updateComReqDto.getEvents());
            }
            compilation.setEvents(events);
        }
        Compilation updatedCompilation = compilationRepository.save(compilation);
        CompilationDto result = compilationMapper.toDto(updatedCompilation);
        log.info("Подборка успешно обновлена");
        return addStats(result);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Попытка получения подборок с параметрами: pinned={}, from={}, size={}", pinned, from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        List<Long> eventIdsFromAllCompilations = compilations.stream()
                .map(Compilation::getEvents)
                .flatMap(Set::stream)
                .map(Event::getId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Long> eventViewsMap = new HashMap<>();
        if (!eventIdsFromAllCompilations.isEmpty()) {
            List<String> allUris = eventIdsFromAllCompilations.stream()
                    .map(id -> "/events/" + id)
                    .collect(Collectors.toList());

            Map<String, Long> viewsFromStats = getViewsFromStats(allUris);

            for (Long eventId : eventIdsFromAllCompilations) {
                String uri = "/events/" + eventId;
                eventViewsMap.put(eventId, viewsFromStats.getOrDefault(uri, 0L));
            }
        }

        List<CompilationDto> result = compilations.stream()
                .map(compilationMapper::toDto)
                .map(compilationDto -> setViewsToEvents(compilationDto, eventViewsMap)) // Используем новый метод
                .collect(Collectors.toList());

        log.debug("Подборки получены. Обработано событий: {}", eventViewsMap.size());
        return result;
    }

    private CompilationDto addStats(CompilationDto compilationDto) {
        if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {

            List<String> uris = compilationDto.getEvents().stream()
                    .map(event -> "/events/" + event.getId())
                    .collect(Collectors.toList());
            Map<String, Long> viewsMap = getViewsFromStats(uris);
            for (EventShortDto eventDto : compilationDto.getEvents()) {
                String eventUri = "/events/" + eventDto.getId();
                eventDto.setViews(viewsMap.getOrDefault(eventUri, 0L));
            }
        }
        return compilationDto;
    }

    private Map<String, Long> getViewsFromStats(List<String> uris) {
        try {
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start = end.minusYears(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            List<ViewStatsDto> stats = statsClient.getStats(
                    start.format(formatter),
                    end.format(formatter),
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

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Поиск подборки с id={}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation", "Id", compId));
        CompilationDto result = compilationMapper.toDto(compilation);
        log.info("Подборка найдена");
        return addStats(result);
    }

    private Set<Event> loadEvents(List<Long> eventIds) {
        List<Event> events = eventRepository.findAllById(eventIds);
        if (events.size() != eventIds.size()) {
            throw new NotFoundException("События не найдены по ID: " + eventIds);
        }
        return new HashSet<>(events);
    }

    @Transactional
    @Override
    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки с id={}", compId);
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation", "Id", compId);
        }
        compilationRepository.deleteById(compId);
        log.info("Подборка с id={} успешно удалена", compId);
    }

    private CompilationDto setViewsToEvents(CompilationDto compilationDto, Map<Long, Long> eventViewsMap) {
        if (compilationDto.getEvents() != null) {
            for (EventShortDto eventShortDto : compilationDto.getEvents()) {
                Long views = eventViewsMap.get(eventShortDto.getId());
                if (views != null) {
                    eventShortDto.setViews(views);
                }
            }
        }
        return compilationDto;
    }
}