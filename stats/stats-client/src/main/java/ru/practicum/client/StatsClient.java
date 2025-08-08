package ru.practicum.client;

import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;

import java.util.List;

public interface StatsClient {

    void saveHit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique);
}