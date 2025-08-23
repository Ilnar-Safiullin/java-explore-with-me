package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 120)
    String title;

    @Column(nullable = false, length = 2000)
    String annotation;

    @Column(nullable = false, length = 7000)
    String description;

    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;

    @Column(name = "created_on", nullable = false)
    LocalDateTime createdOn;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    User initiator;

    @Embedded
    Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    EventState state;

    @Column(nullable = false)
    @Builder.Default
    Boolean paid = false;

    @Column(name = "participant_limit", nullable = false)
    Integer participantLimit;

    @Column(name = "request_moderation", nullable = false)
    Boolean requestModeration;

    @Column(name = "confirmed_requests", nullable = false)
    @Builder.Default
    Integer confirmedRequests = 0;
}