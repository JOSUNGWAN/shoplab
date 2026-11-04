package com.shoplab.domain.product.application;

import com.shoplab.domain.product.domain.AdminProductSearchCondition;
import com.shoplab.domain.category.domain.Category;
import com.shoplab.domain.category.domain.CategoryRepository;
import com.shoplab.domain.product.domain.Product;
import com.shoplab.domain.product.domain.ProductRepository;
import com.shoplab.domain.product.dto.*;
import com.shoplab.global.common.PageResponse;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public PageResponse<AdminProductResponse> search(AdminProductSearchRequest request) {
        AdminProductSearchCondition condition =
                new AdminProductSearchCondition(request.keyword(), request.categoryId(), request.status());
        return PageResponse.from(productRepository
                .searchForAdmin(condition, PageRequest.of(request.page(), request.size()))
                .map(AdminProductResponse::from));
    }

    public AdminProductDetailResponse getProduct(Long productId) {
        return AdminProductDetailResponse.from(findProduct(productId));
    }

    @Transactional
    public AdminProductDetailResponse create(AdminProductCreateRequest request) {
        Product product = Product.create(findCategory(request.categoryId()), request.name(), request.description(),
                request.price(), request.stockQuantity(), request.thumbnailUrl());
        productRepository.save(product);
        log.info("관리자 상품 등록: id={}, name={}", product.getId(), product.getName());
        return AdminProductDetailResponse.from(product);
    }

    @Transactional
    public AdminProductDetailResponse update(Long productId, AdminProductUpdateRequest request) {
        Product product = findProduct(productId);
        product.update(findCategory(request.categoryId()), request.name(), request.description(),
                request.price(), request.thumbnailUrl());
        return AdminProductDetailResponse.from(product);
    }

    @Transactional
    public AdminProductDetailResponse adjustStock(Long productId, AdminStockAdjustRequest request) {
        Product product = findProductForUpdate(productId);
        int before = product.getStockQuantity();
        product.adjustStock(request.delta());
        log.info("관리자 재고 조정: id={}, {} → {} ({}{})", productId, before, product.getStockQuantity(),
                request.delta() > 0 ? "+" : "", request.delta());
        return AdminProductDetailResponse.from(product);
    }

    @Transactional
    public AdminProductDetailResponse changeVisibility(Long productId, AdminVisibilityRequest request) {
        Product product = findProductForUpdate(productId);
        if (request.visible()) {
            product.show();
        } else {
            product.hide();
        }
        return AdminProductDetailResponse.from(product);
    }

    private Product findProduct(Long productId) {
        return productRepository.findWithCategoryById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Product findProductForUpdate(Long productId) {
        return productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}