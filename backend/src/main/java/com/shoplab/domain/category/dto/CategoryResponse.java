package com.shoplab.domain.category.dto;

import java.util.List;

public record CategoryResponse(Long id, String name, List<CategoryResponse> children) {
}