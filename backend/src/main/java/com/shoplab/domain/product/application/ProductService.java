package com.shoplab.domain.product.application;

import com.shoplab.domain.category.domain.CategoryRepository;
import com.shoplab.domain.product.domain.Product;
import com.shoplab.domain.product.domain.ProductRepository;
import com.shoplab.domain.product.domain.ProductSearchCondition;
import com.shoplab.domain.product.domain.ProductStatus;
import com.shoplab.domain.product.dto.ProductDetailResponse;
import com.shoplab.domain.product.dto.ProductSearchRequest;
import com.shoplab.domain.product.dto.ProductSummaryResponse;
import com.shoplab.global.common.PageResponse;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    // 사용자에게 보이는 상태 (숨김 상품 제외, 품절은 표시)
    private static final List<ProductStatus> VISIBLE = List.of(ProductStatus.ON_SALE, ProductStatus.SOLD_OUT);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public PageResponse<ProductSummaryResponse> getProducts(ProductSearchRequest request) {
        if (request.minPrice() != null && request.maxPrice() != null
                && request.minPrice() > request.maxPrice()) {
            throw new BusinessException(ErrorCode.INVALID_PRICE_RANGE);
        }

        ProductSearchCondition condition = new ProductSearchCondition(
                request.categoryId() == null ? null : withChildren(request.categoryId()),
                request.keyword(),
                request.minPrice(),
                request.maxPrice(),
                VISIBLE,
                request.sort());

        Page<Product> page = productRepository.search(condition, PageRequest.of(request.page(), request.size()));
        return PageResponse.from(page.map(ProductSummaryResponse::from));
    }

    public ProductDetailResponse getProduct(Long productId) {
        return productRepository.findWithCategoryById(productId)
                .filter(product -> product.getStatus() != ProductStatus.HIDDEN)
                .map(ProductDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    // "의류"를 고르면 하위 "상의", "하의" 상품까지 함께 조회
    private List<Long> withChildren(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        categoryRepository.findByParentId(categoryId).forEach(child -> ids.add(child.getId()));
        return ids;
    }
}