package com.shoplab.domain.timedeal;

import com.shoplab.domain.product.domain.Product;
import com.shoplab.domain.product.domain.ProductRepository;
import com.shoplab.domain.timedeal.application.stock.AtomicUpdateStockStrategy;
import com.shoplab.domain.timedeal.application.stock.NoLockStockStrategy;
import com.shoplab.domain.timedeal.application.stock.OptimisticLockStockStrategy;
import com.shoplab.domain.timedeal.application.stock.PessimisticLockStockStrategy;
import com.shoplab.domain.timedeal.application.stock.RedisStockStrategy;
import com.shoplab.domain.timedeal.application.stock.TimeDealStockStrategy;
import com.shoplab.domain.timedeal.domain.TimeDeal;
import com.shoplab.domain.timedeal.domain.TimeDealRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.jpa.show-sql=false")
class TimeDealConcurrencyTest {

    private static final int STOCK = 100;        // 한정 수량
    private static final int REQUESTS = 1_000;   // 동시 구매 요청
    private static final int THREADS = 32;       // 동시에 실행할 스레드

    @Autowired private TimeDealRepository timeDealRepository;
    @Autowired private ProductRepository productRepository;

    @Autowired private NoLockStockStrategy noLock;
    @Autowired private PessimisticLockStockStrategy pessimisticLock;
    @Autowired private OptimisticLockStockStrategy optimisticLock;
    @Autowired private AtomicUpdateStockStrategy atomicUpdate;
    @Autowired private RedisStockStrategy redisStock;

    private Long timeDealId;

    @BeforeEach
    void setUp() {
        Product product = productRepository.findAll().getFirst();   // 샘플 상품 하나 사용
        LocalDateTime now = LocalDateTime.now();
        timeDealId = timeDealRepository.save(
                TimeDeal.create(product, 9_900, STOCK, now.minusHours(1), now.plusHours(1))).getId();
    }

    @AfterEach
    void tearDown() {
        redisStock.clearStock(timeDealId);
        timeDealRepository.deleteById(timeDealId);
    }

    @Test
    @DisplayName("락이 없으면 갱신 손실로 한정 수량보다 많이 팔린다")
    void noLock() throws InterruptedException {
        Result result = run(noLock);

        // 판매 성공 수 > 실제로 줄어든 수량 → 초과 판매 발생
        assertThat(result.success()).isGreaterThan(STOCK - result.remaining());
    }

    @Test
    @DisplayName("비관적 락: 정확히 100개만 팔린다")
    void pessimisticLock() throws InterruptedException {
        Result result = run(pessimisticLock);

        assertThat(result.success()).isEqualTo(STOCK);
        assertThat(result.remaining()).isZero();
    }

    @Test
    @DisplayName("낙관적 락: 초과 판매는 없지만 충돌이 많으면 재시도가 몰린다")
    void optimisticLock() throws InterruptedException {
        Result result = run(optimisticLock);

        assertThat(result.success()).isEqualTo(STOCK - result.remaining());   // 판매 수 = 줄어든 수량
        assertThat(result.success()).isLessThanOrEqualTo(STOCK);
    }

    @Test
    @DisplayName("조건부 UPDATE: 정확히 100개만 팔린다")
    void atomicUpdate() throws InterruptedException {
        Result result = run(atomicUpdate);

        assertThat(result.success()).isEqualTo(STOCK);
        assertThat(result.remaining()).isZero();
    }

    @Test
    @DisplayName("Redis 선차감: 정확히 100개만 팔리고, DB에는 100건만 도달한다")
    void redis() throws InterruptedException {
        redisStock.initStock(timeDealId, STOCK);

        Result result = run(redisStock);

        assertThat(result.success()).isEqualTo(STOCK);
        assertThat(result.remaining()).isZero();
    }

    private Result run(TimeDealStockStrategy strategy) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);         // 모든 요청을 동시에 출발시키는 신호
        CountDownLatch done = new CountDownLatch(REQUESTS);   // 모든 요청이 끝날 때까지 대기
        AtomicInteger success = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        for (int i = 0; i < REQUESTS; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    if (strategy.tryDecrease(timeDealId)) success.incrementAndGet();
                    else soldOut.incrementAndGet();
                } catch (Exception e) {
                    failed.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        long begin = System.nanoTime();
        start.countDown();
        done.await();
        long elapsedMs = (System.nanoTime() - begin) / 1_000_000;
        pool.shutdown();

        int remaining = timeDealRepository.findRemainingQuantity(timeDealId);
        Result result = new Result(success.get(), soldOut.get(), failed.get(), remaining, elapsedMs);

        System.out.printf("%n[%s] 판매 성공 %d / 품절 %d / 오류 %d / 남은 수량 %d / %d ms%n%n",
                strategy.name(), result.success(), result.soldOut(), result.failed(),
                result.remaining(), result.elapsedMs());
        return result;
    }

    private record Result(int success, int soldOut, int failed, int remaining, long elapsedMs) {
    }
}