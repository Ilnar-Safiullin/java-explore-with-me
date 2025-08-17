package ru.practicum.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByCategoryId(Long categoryId);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    boolean existsEventByInitiatorId(Long userI);

    @Query(value = """
            SELECT * FROM events
            WHERE initiator_id = :userId
            ORDER BY event_date DESC
            LIMIT :size
            OFFSET :from""",
            nativeQuery = true)
    List<Event> findUserEventsNative(@Param("userId") Long userId, @Param("size") int size, @Param("from") int from);

    @Query("SELECT e FROM Event e " +
           "WHERE e.state = 'PUBLISHED' " +
           "AND (:text IS NULL OR " +
           "LOWER(e.annotation) LIKE '%' || LOWER(CAST(:text AS string)) || '%' OR " +
           "LOWER(e.description) LIKE '%' || LOWER(CAST(:text AS string)) || '%') " +
           "AND (COALESCE(:categories, NULL) IS NULL OR e.category.id IN :categories) " +
           "AND (COALESCE(:paid, NULL) IS NULL OR e.paid = :paid) " +
           "AND (COALESCE(:rangeStart, NULL) IS NULL OR e.eventDate >= :rangeStart) " +
           "AND (COALESCE(:rangeEnd, NULL) IS NULL OR e.eventDate <= :rangeEnd) " +
           "AND (:onlyAvailable = FALSE OR e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)")
    Page<Event> findPublicEvents(@Param("text") String text,
                                 @Param("categories") List<Long> categories,
                                 @Param("paid") Boolean paid,
                                 @Param("rangeStart") LocalDateTime rangeStart,
                                 @Param("rangeEnd") LocalDateTime rangeEnd,
                                 @Param("onlyAvailable") Boolean onlyAvailable,
                                 Pageable pageable);

    @Query("SELECT e FROM Event e " +
           "WHERE (COALESCE(:users, NULL) IS NULL OR e.initiator.id IN :users) " +
           "AND (COALESCE(:states, NULL) IS NULL OR e.state IN :states) " +
           "AND (COALESCE(:categories, NULL) IS NULL OR e.category.id IN :categories) " +
           "AND (COALESCE(:rangeStart, NULL) IS NULL OR e.eventDate >= :rangeStart) " +
           "AND (COALESCE(:rangeEnd, NULL) IS NULL OR e.eventDate <= :rangeEnd)")
    Page<Event> findAdminEvents(@Param("users") List<Long> users,
                                @Param("states") List<EventState> states,
                                @Param("categories") List<Long> categories,
                                @Param("rangeStart") LocalDateTime rangeStart,
                                @Param("rangeEnd") LocalDateTime rangeEnd,
                                Pageable pageable);
}