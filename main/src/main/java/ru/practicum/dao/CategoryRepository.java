package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Category;

import java.util.List;


public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    @Query(value = "SELECT * FROM categories ORDER BY id ASC LIMIT :size OFFSET :from",
            nativeQuery = true)
    List<Category> findCategoriesNative(@Param("from") int from, @Param("size") int size);
}