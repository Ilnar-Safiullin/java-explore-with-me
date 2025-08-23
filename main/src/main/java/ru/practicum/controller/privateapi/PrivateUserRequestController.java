package ru.practicum.controller.privateapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateUserRequestController {
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto add(@PathVariable Long userId,
                                       @RequestParam Long eventId) {
        return requestService.addRequest(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> getAllUserRequests(@PathVariable Long userId) {
        return requestService.getAllUserRequests(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelUserRequest(@PathVariable Long userId,
                                                     @PathVariable Long requestId) {
        return requestService.cancelUserRequest(userId, requestId);
    }
}