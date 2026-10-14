package com.shoplab.domain.cart.application;

import com.shoplab.domain.cart.domain.CartItem;
import com.shoplab.domain.cart.domain.CartItemRepository;
import com.shoplab.domain.cart.dto.AddCartItemRequest;
import com.shoplab.domain.category.domain.Category;
import com.shoplab.domain.member.domain.Member;
import com.shoplab.domain.member.domain.MemberRepository;
import com.shoplab.domain.product.domain.Product;
import com.shoplab.domain.product.domain.ProductRepository;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private MemberRepository memberRepository;

    @InjectMocks private CartService cartService;

    private final Category category = Category.create("상의", null, 1);
    private final Product product = Product.create(category, "티셔츠", "설명", 19000, 10, null);
    private final Member member = Member.create("test@shoplab.com", "encoded", "홍길동", null);

    @Test
    @DisplayName("이미 담긴 상품을 다시 담으면 새로 저장하지 않고 수량을 합친다")
    void addSameProductMergesQuantity() {
        CartItem existing = CartItem.create(member, product, 2);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(cartItemRepository.findByMemberIdAndProductId(any(), any())).willReturn(Optional.of(existing));

        cartService.addItem(1L, new AddCartItemRequest(1L, 3));

        assertThat(existing.getQuantity()).isEqualTo(5);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("합친 수량이 재고를 넘으면 OUT_OF_STOCK 예외가 발생한다")
    void addOverStockThrows() {
        CartItem existing = CartItem.create(member, product, 8);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(cartItemRepository.findByMemberIdAndProductId(any(), any())).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> cartService.addItem(1L, new AddCartItemRequest(1L, 3)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OUT_OF_STOCK);

        assertThat(existing.getQuantity()).isEqualTo(8); // 실패하면 기존 수량 그대로
    }
}