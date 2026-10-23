package com.shoplab.domain.timedeal.application;

import com.shoplab.domain.timedeal.domain.TimeDeal;
import com.shoplab.domain.timedeal.domain.TimeDealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealStockService {

    public static final long SOLD_OUT = -1;
    public static final long ALREADY_PURCHASED = -2;

    // 구매 이력 확인 → 수량 확인 → 차감 → 구매자 기록을 한 번에 (Redis는 스크립트를 중간에 끊지 않음)
    private static final RedisScript<Long> RESERVE = RedisScript.of("""
            if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
              return -2
            end
            local left = tonumber(redis.call('GET', KEYS[1]) or '0')
            if left <= 0 then
              return -1
            end
            redis.call('DECR', KEYS[1])
            redis.call('SADD', KEYS[2], ARGV[1])
            redis.call('PEXPIRE', KEYS[2], ARGV[2])
            return left - 1
            """, Long.class);

    // 구매자 기록이 있을 때만 수량 +1 (두 번 호출돼도 한 번만 반환)
    private static final RedisScript<Long> RELEASE = RedisScript.of("""
            if redis.call('SREM', KEYS[2], ARGV[1]) == 1 then
              redis.call('INCR', KEYS[1])
              return 1
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final TimeDealRepository timeDealRepository;

    /** Redis에 수량이 없으면 DB 남은 수량으로 채운다 (여러 요청이 동시에 와도 한 번만 설정) */
    public void ensureLoaded(TimeDeal timeDeal) {
        redisTemplate.opsForValue().setIfAbsent(
                stockKey(timeDeal.getId()), String.valueOf(timeDeal.getRemainingQuantity()), ttl(timeDeal));
    }

    /** 남은 수량(0 이상), SOLD_OUT, ALREADY_PURCHASED 중 하나를 반환 */
    public long reserve(TimeDeal timeDeal, Long memberId) {
        Long result = redisTemplate.execute(RESERVE,
                List.of(stockKey(timeDeal.getId()), buyersKey(timeDeal.getId())),
                String.valueOf(memberId), String.valueOf(ttl(timeDeal).toMillis()));
        return result == null ? SOLD_OUT : result;
    }

    /** Redis 차감만 되돌린다 (주문 생성 실패 시) */
    public void cancelReservation(Long timeDealId, Long memberId) {
        redisTemplate.execute(RELEASE, List.of(stockKey(timeDealId), buyersKey(timeDealId)),
                String.valueOf(memberId));
    }

    /** 주문 취소 트랜잭션 안에서 호출: DB 수량은 지금, Redis는 커밋이 끝난 뒤 반환 */
    public void release(Long timeDealId, Long memberId) {
        timeDealRepository.increaseIfBelowTotal(timeDealId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    cancelReservation(timeDealId, memberId);
                } catch (Exception e) {
                    log.error("[수동 처리 필요] 타임딜 Redis 수량 반환 실패. timeDealId={}, memberId={}",
                            timeDealId, memberId, e);
                }
            }
        });
    }

    private Duration ttl(TimeDeal timeDeal) {
        Duration ttl = Duration.between(LocalDateTime.now(), timeDeal.getEndAt().plusDays(1));
        return ttl.isNegative() || ttl.isZero() ? Duration.ofMinutes(1) : ttl;
    }

    private String stockKey(Long timeDealId) {
        return "timedeal:" + timeDealId + ":stock";
    }

    private String buyersKey(Long timeDealId) {
        return "timedeal:" + timeDealId + ":buyers";
    }
}