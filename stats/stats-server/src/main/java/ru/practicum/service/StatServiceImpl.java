package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StatRepository statRepository;
    private final EndpointHitMapper endpointHitMapper;

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
        LocalDateTime startTime = LocalDateTime.parse(start, FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(end, FORMATTER);

        return unique ?
                statRepository.findUniqueStats(startTime, endTime, uris)
                : statRepository.findAllStats(startTime, endTime, uris);
    }
}