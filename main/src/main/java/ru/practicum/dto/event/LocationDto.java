package ru.practicum.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    @NotNull
    private Float lat;

    @NotNull
    private Float lon;
}