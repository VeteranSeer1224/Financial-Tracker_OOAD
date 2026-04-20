package com.finance.tracker.service;

import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.model.entity.Category;
import com.finance.tracker.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<Category> getUserCategories(UUID userId) {
        return categoryRepository.findByUserUserId(userId);
    }

    @Transactional
    public Category updateBudgetLimit(UUID userId, UUID categoryId, double budgetLimit) {
        Category category = categoryRepository.findByCategoryIdAndUserUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        category.setBudgetLimit(budgetLimit);
        return categoryRepository.save(category);
    }
}
