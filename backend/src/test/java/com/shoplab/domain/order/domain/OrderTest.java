package com.shoplab.domain.order.domain;

import com.shoplab.domain.category.domain.Category;
import com.shoplab.domain.member.domain.Member;
import com.shoplab.domain.product.domain.Product;
import com.shoplab.domain.product.domain.ProductStatus;
import com.shoplab.global.exception.BusinessException;
import com.shoplab.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private final Category category = Category.create("상의", null, 1);
    private final Member member = Member.create("test@shoplab.com", "encoded", "홍길동", null);
    private final ShippingInfo shipping = new ShippingInfo("홍길동", "010-1234-5678", "12345", "서울시", null);

    private Order orderOf(Product product, int quantity) {
        product.decreaseStock(quantity);
        return Order.create(member, shipping, List.of(OrderItem.create(product, quantity)));
    }

    @Test
    @DisplayName("주문 생성 시 결제 대기 상태이고, 주문 시점 가격으로 총액을 계산한다")
    void createOrder() {
        Product product = Product.create(category, "티셔츠", "설명", 19000, 10, null);

        Order order = orderOf(product, 3);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getTotalAmount()).isEqualTo(57000);
        assertThat(order.getItems().getFirst().getOrderPrice()).isEqualTo(19000);
        assertThat(product.getStockQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("재고를 모두 주문하면 상품이 품절되고, 취소하면 재고가 돌아와 다시 판매 중이 된다")
    void cancelRestoresStock() {
        Product product = Product.create(category, "티셔츠", "설명", 19000, 2, null);
        Order order = orderOf(product, 2);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(product.getStockQuantity()).isEqualTo(2);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);
    }

    @Test
    @DisplayName("배송 준비가 시작된 주문은 사용자가 취소할 수 없다")
    void cannotCancelAfterPreparing() {
        Order order = orderOf(Product.create(category, "티셔츠", "설명", 19000, 10, null), 1);
        order.changeStatus(OrderStatus.PAID);
        order.changeStatus(OrderStatus.PREPARING);

        assertThatThrownBy(order::cancel)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_CANNOT_BE_CANCELED);
    }

    @Test
    @DisplayName("허용되지 않은 상태 변경은 막는다 (결제 대기 → 배송 중)")
    void invalidTransition() {
        Order order = orderOf(Product.create(category, "티셔츠", "설명", 19000, 10, null), 1);

        assertThatThrownBy(() -> order.changeStatus(OrderStatus.SHIPPING))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_ORDER_STATUS);
    }

    @Test
    @DisplayName("재고보다 많이 주문하면 OUT_OF_STOCK 예외가 발생한다")
    void outOfStock() {
        Product product = Product.create(category, "티셔츠", "설명", 19000, 1, null);

        assertThatThrownBy(() -> product.decreaseStock(2))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("이미 취소된 주문을 다시 취소하면 ORDER_ALREADY_CANCELED 예외가 발생한다")
    void cancelTwice() {
        Order order = orderOf(Product.create(category, "티셔츠", "설명", 19000, 10, null), 1);
        order.cancel();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_ALREADY_CANCELED);
    }
}