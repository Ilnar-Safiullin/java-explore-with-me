package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointHitDto {

    @NotBlank(message = "App не может быть пустым")
    @Size(min = 1, max = 32, message = "Кол-во символов в поле app от 1 до 32")
    private String app;

    @NotBlank(message = "Uri не может быть пустым")
    @Size(min = 1, max = 128, message = "Кол-во символов в поле uri от 1 до 128 символов")
    private String uri;

    @NotBlank(message = "Ip не может быть пустым")
    @Size(min = 7, max = 16, message = "Кол-во символов в поле ip от 7 до 16 символов")
    private String ip;

    @NotNull(message = "Timestamp не может быть пустым")
    @PastOrPresent(message = "Timestamp не может быть из будущего")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}