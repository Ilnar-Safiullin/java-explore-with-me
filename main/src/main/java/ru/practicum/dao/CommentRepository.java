package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByAuthorId(Long authorId);

    List<Comment> findAllByEventId(Long eventId);

    List<Comment> findAllByEventIdIn(List<Long> eventId);
}