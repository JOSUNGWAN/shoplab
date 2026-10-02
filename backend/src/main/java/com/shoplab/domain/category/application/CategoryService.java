package com.shoplab.domain.category.application;

import com.shoplab.domain.category.domain.Category;
import com.shoplab.domain.category.domain.CategoryRepository;
import com.shoplab.domain.category.dto.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getCategoryTree() {
        List<Category> all = categoryRepository.findAllByOrderBySortOrderAsc();

        Map<Long, List<Category>> childrenByParentId = all.stream()
                .filter(category -> category.getParent() != null)
                .collect(Collectors.groupingBy(category -> category.getParent().getId()));

        return all.stream()
                .filter(category -> category.getParent() == null)
                .map(root -> new CategoryResponse(
                        root.getId(),
                        root.getName(),
                        childrenByParentId.getOrDefault(root.getId(), List.of()).stream()
                                .map(child -> new CategoryResponse(child.getId(), child.getName(), List.of()))
                                .toList()))
                .toList();
    }
}