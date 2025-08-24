package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.RequestCommentDto;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", source = "requestCommentDto.description")
    @Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    Comment toEntity(RequestCommentDto requestCommentDto, User author, Event event);

    @Mapping(target = "eventId", source = "comment.event.id")
    @Mapping(target = "authorId", source = "comment.author.id")
    CommentDto toDto(Comment comment);

}