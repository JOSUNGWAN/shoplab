package com.shoplab.domain.timedeal.application.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisStockStrategy implements TimeDealStockStrategy {

    private final StringRedisTemplate redisTemplate;
    private final AtomicUpdateStockStrategy atomicUpdateStockStrategy;

    @Override
    public String name() {
        return "Redis 선차감";
    }

    /** 타임딜 오픈 전에 Redis에 수량을 올려 둔다 */
    public void initStock(Long timeDealId, int quantity) {
        redisTemplate.opsForValue().set(key(timeDealId), String.valueOf(quantity));
    }

    public void clearStock(Long timeDealId) {
        redisTemplate.delete(key(timeDealId));
    }

    @Override
    public boolean tryDecrease(Long timeDealId) {
        // ① Redis에서 원자적으로 1 감소 (싱글 스레드라 동시 요청도 순서대로 처리)
        Long left = redisTemplate.opsForValue().decrement(key(timeDealId));
        if (left == null || left < 0) {
            return false;   // 품절 → DB까지 가지 않음
        }
        // ② 통과한 요청(최대 100건)만 DB에 반영
        boolean saved = atomicUpdateStockStrategy.tryDecrease(timeDealId);
        if (!saved) {
            redisTemplate.opsForValue().increment(key(timeDealId));   // DB 반영 실패 시 되돌리기
        }
        return saved;
    }

    private String key(Long timeDealId) {
        return "timedeal:" + timeDealId + ":remaining";
    }
}