package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class StatsClientImpl implements StatsClient {
    private final RestClient restClient;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClientImpl(@Value("${stats-server.url:http://localhost:9090}") String statsServerUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(statsServerUrl)
                .build();
    }

    @Override
    public void saveHit(EndpointHitDto endpointHitDto) {
        log.info("Добавление статистики (отправление клиентом): {}", endpointHitDto);
        restClient.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHitDto)
                .retrieve()
                .toBodilessEntity();
        log.info("Статистика добавлена");
    }

    @Override
    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        log.info("Получение статистики с параметрами start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        validateTimeRange(start, end);
        List<ViewStatsDto> stats = restClient.get()
                .uri(uriBuilder -> buildStatsUri(uriBuilder, start, end, uris, unique))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        log.info("Статистика получена: {}", stats);
        return stats;
    }

    private void validateTimeRange(String start, String end) {
        Objects.requireNonNull(start, "Start date cannot be null");
        Objects.requireNonNull(end, "End date cannot be null");

        LocalDateTime startTime = LocalDateTime.parse(start, DATE_TIME_FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(end, DATE_TIME_FORMATTER);

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    private URI buildStatsUri(UriBuilder uriBuilder, String start, String end,
                              List<String> uris, Boolean unique) {
        UriBuilder builder = uriBuilder.path("/stats")
                .queryParam("start", start)
                .queryParam("end", end);
        if (uris != null && !uris.isEmpty()) {
            uris.forEach(uri -> builder.queryParam("uris", uri));
        }
        if (unique != null) {
            builder.queryParam("unique", unique);
        }
        return builder.build();
    }
}