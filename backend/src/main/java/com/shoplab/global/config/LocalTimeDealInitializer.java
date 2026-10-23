package com.shoplab.global.config;

import com.shoplab.domain.product.domain.Product;
import com.shoplab.domain.product.domain.ProductRepository;
import com.shoplab.domain.timedeal.domain.TimeDeal;
import com.shoplab.domain.timedeal.domain.TimeDealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("local")
@Order(2)
@RequiredArgsConstructor
public class LocalTimeDealInitializer implements ApplicationRunner {

    private final TimeDealRepository timeDealRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        LocalDateTime now = LocalDateTime.now();
        if (!timeDealRepository.findByEndAtAfterOrderByStartAtAsc(now).isEmpty()) {
            return; // 진행 중이거나 예정된 타임딜이 있으면 건너뜀
        }

        Map<String, Product> products = productRepository.findAll().stream()
                .collect(Collectors.toMap(Product::getName, Function.identity(), (a, b) -> a));

        create(products.get("기계식 키보드"), 49_000, 5, now.minusMinutes(1), now.plusHours(3));  // 진행 중
        create(products.get("오버핏 후드티"), 29_000, 3, now.plusMinutes(10), now.plusHours(3)); // 10분 뒤 오픈
        log.info("로컬 샘플 타임딜 생성 완료");
    }

    private void create(Product product, int dealPrice, int quantity, LocalDateTime startAt, LocalDateTime endAt) {
        if (product != null) {
            timeDealRepository.save(TimeDeal.create(product, dealPrice, quantity, startAt, endAt));
        }
    }
}