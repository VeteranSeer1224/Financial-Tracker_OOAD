package com.finance.tracker.repository;

import com.finance.tracker.model.entity.Category;
import com.finance.tracker.model.enums.CategoryType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByUserUserId(UUID userId);

    Optional<Category> findByUserUserIdAndType(UUID userId, CategoryType type);

    Optional<Category> findByCategoryIdAndUserUserId(UUID categoryId, UUID userId);
}
