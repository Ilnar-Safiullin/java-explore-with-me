package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dao.CommentRepository;
import ru.practicum.dao.EventRepository;
import ru.practicum.dao.RequestRepository;
import ru.practicum.dao.UserRepository;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.RequestCommentDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.service.CommentService;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final CommentMapper commentMapper;


    @Override
    public CommentDto addComment(Long userId, Long eventId, RequestCommentDto requestCommentDto) {
        log.info("Добавление комментария юзером с id {} к евенту с id {}", userId, eventId);
        User author = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", "id", userId));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event", "id", eventId));
        if (!eventRepository.existsPublishedEvent(eventId)) {
            throw new ConflictException("Event не был опубликован");
        }
        Comment comment = commentMapper.toEntity(requestCommentDto, author, event);
        commentRepository.save(comment);
        log.debug("Комментарий добавлен {}", comment);
        return commentMapper.toDto(comment);
    }

    @Override
    public CommentDto updateCommentByUser(Long userId, Long commentId, RequestCommentDto dto) {
        log.info("Обновления комментария с Id: {}, юзером с id {}", commentId, userId);
        User author = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", "id", userId));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment", "id", commentId));
        if (!comment.getAuthor().getId().equals(author.getId())) {
            throw new ConflictException("У данного комментария другой автор");
        }
        comment.setDescription(dto.getDescription());
        commentRepository.save(comment);
        log.debug("Комментарий обновлен {}", comment);
        return commentMapper.toDto(comment);
    }

    @Override
    public List<CommentDto> getCommentsByUser(Long userId) {
        log.info("Получение всех комментов оставленных юзером с id {}", userId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", "id", userId));
        List<Comment> comments = commentRepository.findAllByAuthorId(userId);
        log.debug("Комментарии юзера получены, кол-во {}", comments.size());
        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void removeComment(Long userId, Long commentId) {
        log.info("Удаление коммента с id: {}, юзером с id {}", commentId, userId);
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", "id", userId));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment", "id", commentId));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("У данного комментария другой автор");
        }
        commentRepository.deleteById(commentId);
        log.debug("Комментарий удален, id {}", commentId);
    }

    @Override
    public List<CommentDto> getCommentsByEvent(Long eventId) {
        log.info("Получение комментариев Евента с id {}", eventId);
        eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event", "id", eventId));
        List<Comment> comments = commentRepository.findAllByEventId(eventId);
        log.debug("Комментарии получены, кол-во {}", comments.size());
        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        log.info("Удаление коммента с id: {}, Админом", commentId);
        commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment", "id", commentId));
        commentRepository.deleteById(commentId);
        log.debug("Комментарий удален");
    }
}