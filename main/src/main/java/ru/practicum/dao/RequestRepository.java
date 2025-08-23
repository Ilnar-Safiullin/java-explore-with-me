package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByEventId(Long eventId);

    @Query("SELECT CASE WHEN :limit = 0 THEN false " +
           "ELSE (SELECT COUNT(r) >= :limit FROM Request r " +
           "WHERE r.event.id = :eventId AND r.status = 'CONFIRMED') END")
    boolean isParticipantLimitReached(@Param("eventId") Long eventId, @Param("limit") Integer limit);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    int countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    List<Request> findAllByRequesterId(Long requesterId);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);
}