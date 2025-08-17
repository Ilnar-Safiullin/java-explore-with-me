package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "events")
@EqualsAndHashCode(of = {"id"})
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pinned", nullable = false)
    private boolean pinned;

    @Column(name = "title", nullable = false, unique = true, length = 128)
    private String title;

    @ManyToMany(fetch = FetchType.LAZY)
    @Builder.Default
    @JoinTable(name = "compilation_events", joinColumns = @JoinColumn(name = "compilation_id"), inverseJoinColumns = @JoinColumn(name = "event_id"))
    private Set<Event> events = new HashSet<>();
}