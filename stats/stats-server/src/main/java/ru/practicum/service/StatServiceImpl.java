package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MissingServletRequestParameterException;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.dao.StatRepository;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatServiceImpl implements StatService {
    private final StatRepository statRepository;
    private final EndpointHitMapper endpointHitMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    @Override
    public void save(EndpointHitDto endpointHitDto) {
        log.debug("Попытка сохранить просмотр: {}", endpointHitDto);
        if (endpointHitDto == null) {
            log.warn("Невозможно сохранить просмотр — параметр EndpointHitDto равен null.");
            throw new IllegalArgumentException("Параметр EndpointHitDto не может быть null.");
        }
        EndpointHit endpointHit = endpointHitMapper.toEntity(endpointHitDto);
        statRepository.save(endpointHit);
        log.info("Просмотр успешно сохранен");
    }

    @Override
    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        log.info("Попытка получить статистику просмотров");

        LocalDateTime startTime = LocalDateTime.parse(start,DATE_TIME_FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(end,DATE_TIME_FORMATTER);

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Старт не может быть позже окончания");
        }

        List<ViewStatsDto> viewStatsDtos;
        if (unique) {
            viewStatsDtos = statRepository.findUniqueStats(startTime, endTime, uris);
        } else {
            viewStatsDtos = statRepository.findAllStats(startTime, endTime, uris);
        }
        log.debug("Статистика получена разамер {}", viewStatsDtos.size());
        return viewStatsDtos;
    }
}