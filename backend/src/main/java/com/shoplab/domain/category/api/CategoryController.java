package com.shoplab.domain.category.api;

import com.shoplab.domain.category.application.CategoryService;
import com.shoplab.domain.category.dto.CategoryResponse;
import com.shoplab.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getCategories() {
        return ApiResponse.ok(categoryService.getCategoryTree());
    }
}