package ru.practicum.service;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.RequestCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addComment(Long userId, Long eventId, RequestCommentDto requestCommentDto);

    CommentDto updateCommentByUser(Long userId, Long commentId, RequestCommentDto requestCommentDto);

    List<CommentDto> getCommentsByEvent(Long eventId);

    List<CommentDto> getCommentsByUser(Long userId);

    void removeComment(Long userId, Long commentId);

    void deleteCommentByAdmin(Long commentId);
}
