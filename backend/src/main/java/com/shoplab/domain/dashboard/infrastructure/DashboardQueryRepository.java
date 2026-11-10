package com.shoplab.domain.dashboard.infrastructure;

import com.shoplab.domain.dashboard.dto.DailySalesResponse;
import com.shoplab.domain.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 대시보드 통계 전용 조회.
 * 집계 결과는 엔티티가 아니므로 JPA 대신 SQL로 직접 조회한다. (JPA와 같은 커넥션·트랜잭션 사용)
 */
@Repository
@RequiredArgsConstructor
public class DashboardQueryRepository {

    private static final List<String> SALES_STATUSES =
            OrderStatus.SALES_STATUSES.stream().map(Enum::name).toList();

    private final NamedParameterJdbcTemplate jdbc;

    public record SalesTotal(long orderCount, long salesAmount) {
    }

    public record ProductStockCount(long soldOut, long lowStock) {
    }

    public record TopProductRow(Long productId, String productName, long quantity, long salesAmount) {
    }

    /** 기간 내 매출 합계 [from, to) */
    public SalesTotal findSalesTotal(LocalDateTime from, LocalDateTime to) {
        String sql = """
                select count(*) as order_count,
                       nvl(sum(total_amount), 0) as sales_amount
                from orders
                where status in (:statuses)
                  and ordered_at >= :from and ordered_at < :to
                """;
        return jdbc.queryForObject(sql, salesParams(from, to),
                (rs, rowNum) -> new SalesTotal(rs.getLong("order_count"), rs.getLong("sales_amount")));
    }

    /** 일별 매출 [from, to). 주문이 없는 날은 결과에 없음 */
    public List<DailySalesResponse> findDailySales(LocalDateTime from, LocalDateTime to) {
        String sql = """
                select to_char(ordered_at, 'YYYY-MM-DD') as sales_date,
                       count(*) as order_count,
                       sum(total_amount) as sales_amount
                from orders
                where status in (:statuses)
                  and ordered_at >= :from and ordered_at < :to
                group by to_char(ordered_at, 'YYYY-MM-DD')
                order by sales_date
                """;
        return jdbc.query(sql, salesParams(from, to), (rs, rowNum) -> new DailySalesResponse(
                LocalDate.parse(rs.getString("sales_date")),
                rs.getLong("order_count"),
                rs.getLong("sales_amount")));
    }

    /** 상품별 판매 순위 [from, to): 판매 수량 → 매출 → 상품 번호 순 */
    public List<TopProductRow> findTopProducts(LocalDateTime from, LocalDateTime to, int limit) {
        String sql = """
                select p.id as product_id,
                       p.name as product_name,
                       sum(oi.quantity) as quantity,
                       sum(oi.order_price * oi.quantity) as sales_amount
                from order_items oi
                join orders o on o.id = oi.order_id
                join products p on p.id = oi.product_id
                where o.status in (:statuses)
                  and o.ordered_at >= :from and o.ordered_at < :to
                group by p.id, p.name
                order by quantity desc, sales_amount desc, p.id
                fetch first :limit rows only
                """;
        MapSqlParameterSource params = salesParams(from, to).addValue("limit", limit);
        return jdbc.query(sql, params, (rs, rowNum) -> new TopProductRow(
                rs.getLong("product_id"),
                rs.getString("product_name"),
                rs.getLong("quantity"),
                rs.getLong("sales_amount")));
    }

    /** 상태별 주문 수 (주문이 없는 상태는 결과에 없음) */
    public Map<OrderStatus, Long> countOrdersByStatus() {
        String sql = "select status, count(*) as cnt from orders group by status";
        return jdbc.query(sql, Map.of(), (rs, rowNum) ->
                        Map.entry(OrderStatus.valueOf(rs.getString("status")), rs.getLong("cnt")))
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /** 품절 상품 수, 재고 부족(판매 중 + 기준 이하) 상품 수 */
    public ProductStockCount countProductStock(int lowStockThreshold) {
        String sql = """
                select sum(case when status = 'SOLD_OUT' then 1 else 0 end) as sold_out,
                       sum(case when status = 'ON_SALE' and stock_quantity <= :threshold then 1 else 0 end) as low_stock
                from products
                """;
        return jdbc.queryForObject(sql, Map.of("threshold", lowStockThreshold),
                (rs, rowNum) -> new ProductStockCount(rs.getLong("sold_out"), rs.getLong("low_stock")));
    }

    private MapSqlParameterSource salesParams(LocalDateTime from, LocalDateTime to) {
        return new MapSqlParameterSource()
                .addValue("statuses", SALES_STATUSES)
                .addValue("from", from)
                .addValue("to", to);
    }
}