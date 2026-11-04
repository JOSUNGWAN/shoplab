package com.shoplab.domain.product.domain;

import com.shoplab.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductAdminTest {

    // 이 테스트는 카테고리가 필요 없어서 null
    private Product productWithStock(int stock) {
        return Product.create(null, "티셔츠", "설명", 19000, stock, null);
    }

    @Test
    @DisplayName("재고를 0으로 차감하면 품절, 다시 입고하면 판매 중이 된다")
    void adjustStockChangesStatus() {
        Product product = productWithStock(3);

        product.adjustStock(-3);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);

        product.adjustStock(10);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);
        assertThat(product.getStockQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("재고보다 많이 차감할 수 없다")
    void cannotAdjustBelowZero() {
        Product product = productWithStock(3);

        assertThatThrownBy(() -> product.adjustStock(-4)).isInstanceOf(BusinessException.class);
        assertThat(product.getStockQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("숨긴 상품은 재고가 늘거나 주문 취소로 복구돼도 숨김이 유지된다")
    void hiddenStaysHidden() {
        Product product = productWithStock(0);
        product.hide();

        product.adjustStock(5);
        product.increaseStock(1);   // 주문 취소로 재고 복구

        assertThat(product.getStatus()).isEqualTo(ProductStatus.HIDDEN);
    }

    @Test
    @DisplayName("다시 보이기를 하면 재고에 따라 판매 중 또는 품절이 된다")
    void showByStock() {
        Product soldOut = productWithStock(0);
        soldOut.hide();
        soldOut.show();
        assertThat(soldOut.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);

        Product onSale = productWithStock(5);
        onSale.hide();
        onSale.show();
        assertThat(onSale.getStatus()).isEqualTo(ProductStatus.ON_SALE);
    }
}