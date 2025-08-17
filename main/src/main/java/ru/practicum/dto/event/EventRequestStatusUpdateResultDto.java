package ru.practicum.dto.event;

import lombok.*;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateResultDto {

    @Builder.Default
    private List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();

    @Builder.Default
    private List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
}