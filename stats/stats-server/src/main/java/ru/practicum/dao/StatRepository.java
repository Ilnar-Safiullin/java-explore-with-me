package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ViewStatsDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
            SELECT new ru.practicum.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip))
            FROM EndpointHit e
            WHERE e.timestamp BETWEEN :start AND :end
            AND (:uris IS NULL OR e.uri IN :uris)
            GROUP BY e.app, e.uri
            ORDER BY COUNT(DISTINCT e.ip) DESC
            """)
    List<ViewStatsDto> findUniqueStats(@Param("start") LocalDateTime start,
                                       @Param("end")LocalDateTime end,
                                       @Param("uris")List<String> uris);

    @Query("""
            SELECT new ru.practicum.ViewStatsDto(e.app, e.uri, COUNT(e.ip))
            FROM EndpointHit e
            WHERE e.timestamp BETWEEN :start AND :end
            AND (:uris IS NULL OR e.uri IN :uris)
            GROUP BY e.app, e.uri
            ORDER BY COUNT(e.ip) DESC
            """)
    List<ViewStatsDto> findAllStats(@Param("start")LocalDateTime start,
                                    @Param("end")LocalDateTime end,
                                    @Param("uris")List<String> uris);
}