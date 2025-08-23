package ru.practicum.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Compilation;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Compilation c WHERE LOWER(TRIM(c.title)) = LOWER(TRIM(:title))")
    boolean existsCompilationByTitle(@Param("title") String title);

    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    Page<Compilation> findByPinned(Boolean pinned, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    Page<Compilation> findAll(Pageable pageable);

}