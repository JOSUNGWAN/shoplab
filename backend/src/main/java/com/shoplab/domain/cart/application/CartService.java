package com.shoplab.domain.cart.application;

import com.shoplab.domain.cart.domain.CartItem;
import com.shoplab.domain.cart.domain.CartItemRepository;
import com.shoplab.domain.cart.dto.AddCartItemRequest;
import com.shoplab.domain.cart.dto.CartCountResponse;
import com.shoplab.domain.cart.dto.CartItemResponse;
import com.shoplab.domain.cart.dto.CartResponse;
import com.shoplab.domain.member.domain.MemberRepository;
import com.shoplab.domain.product.domain.Product;
import com.shoplab.domain.product.domain.ProductRepository;
import com.shoplab.domain.product.domain.ProductStatus;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private static final int MAX_QUANTITY = 99;

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    public CartResponse getCart(Long memberId) {
        return CartResponse.from(cartItemRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId));
    }

    public CartCountResponse getCount(Long memberId) {
        return new CartCountResponse(cartItemRepository.countByMemberId(memberId));
    }

    @Transactional
    public CartItemResponse addItem(Long memberId, AddCartItemRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        validateOnSale(product);

        CartItem cartItem = cartItemRepository.findByMemberIdAndProductId(memberId, product.getId())
                .map(existing -> {
                    // 이미 담긴 상품 → 수량 합치기 (save 호출 없이 변경 감지로 UPDATE)
                    int newQuantity = existing.getQuantity() + request.quantity();
                    validateQuantity(product, newQuantity);
                    existing.changeQuantity(newQuantity);
                    return existing;
                })
                .orElseGet(() -> {
                    validateQuantity(product, request.quantity());
                    return cartItemRepository.save(CartItem.create(
                            memberRepository.getReferenceById(memberId), product, request.quantity()));
                });

        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public CartItemResponse changeQuantity(Long memberId, Long cartItemId, int quantity) {
        CartItem cartItem = findOwnedItem(memberId, cartItemId);
        validateOnSale(cartItem.getProduct());
        validateQuantity(cartItem.getProduct(), quantity);
        cartItem.changeQuantity(quantity);
        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public void removeItem(Long memberId, Long cartItemId) {
        cartItemRepository.delete(findOwnedItem(memberId, cartItemId));
    }

    @Transactional
    public void removeItems(Long memberId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return;
        }
        cartItemRepository.deleteByIdsAndMemberId(cartItemIds, memberId);
    }

    private CartItem findOwnedItem(Long memberId, Long cartItemId) {
        return cartItemRepository.findByIdAndMemberId(cartItemId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    private void validateOnSale(Product product) {
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }
    }

    private void validateQuantity(Product product, int quantity) {
        if (quantity > MAX_QUANTITY) {
            throw new BusinessException(ErrorCode.CART_QUANTITY_EXCEEDED);
        }
        if (quantity > product.getStockQuantity()) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
    }
}