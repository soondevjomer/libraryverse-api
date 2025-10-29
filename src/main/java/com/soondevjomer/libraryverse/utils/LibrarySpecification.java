package com.soondevjomer.libraryverse.utils;

import com.soondevjomer.libraryverse.constant.OrderStatus;
import com.soondevjomer.libraryverse.constant.SortBy;
import com.soondevjomer.libraryverse.constant.SortDirection;
import com.soondevjomer.libraryverse.dto.FilterDto;
import com.soondevjomer.libraryverse.model.Library;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class LibrarySpecification {
    public static Specification<Library> filterLibraries(FilterDto filter) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            log.info("Applying search filter for libraries");
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String keyword = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("name")), keyword),
                                cb.like(cb.lower(root.get("address")), keyword),
                                cb.like(cb.lower(root.get("description")), keyword)
                        )
                );
            }

            log.info("user library id is {}", filter.getLibraryId());
            if (filter.getLibraryId() != null && filter.getLibraryId() != 0) {
                log.info("Applying library ID filter: {}", filter.getLibraryId());
                predicates.add(cb.equal(root.get("id"), filter.getLibraryId()));
            }

            if (!predicates.isEmpty()) {
                query.where(cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            log.info("Applying sort settings for libraries");
            if (filter.getSortBy() == null) filter.setSortBy(SortBy.CREATED_DATE);
            if (filter.getSortDirection() == null) filter.setSortDirection(SortDirection.ASC);

            Expression<?> sortExpr;

            log.info("Sorting by: {}", filter.getSortBy());
            switch (filter.getSortBy()) {
                case CREATED_DATE -> sortExpr = root.get("createdDate");
                case NAME -> sortExpr = root.get("name");
                case POPULAR_TEST -> {
                    log.info("Sorting by library popularity (based on number of books)");
                    var booksJoin = root.join("books", JoinType.LEFT);
                    sortExpr = cb.count(booksJoin);
                    query.groupBy(root.get("id"));
                }
                case POPULAR -> {
                    log.info("Calculating library popularity: (viewCount * 0.1) + (totalRevenue * 0.6) + (totalDelivered * 0.3)");
                    Join<Object, Object> storeOrderJoin = root.join("storeOrders", JoinType.LEFT);
                    Join<Object, Object> orderItemJoin = storeOrderJoin.join("orderItems", JoinType.LEFT);

                    Expression<Double> viewExpr = cb.coalesce(cb.toDouble(root.get("viewCount")), 0.0);

                    Expression<Double> revenueExpr = cb.sum(
                            cb.<Double>selectCase()
                                    .when(cb.equal(storeOrderJoin.get("orderStatus"), OrderStatus.DELIVERED),
                                            cb.coalesce(cb.toDouble(storeOrderJoin.get("subtotal")), 0.0))
                                    .otherwise(0.0)
                    );

                    Expression<Double> deliveredExpr = cb.sum(
                            cb.<Double>selectCase()
                                    .when(cb.equal(storeOrderJoin.get("orderStatus"), OrderStatus.DELIVERED),
                                            cb.coalesce(cb.toDouble(orderItemJoin.get("quantity")), 0.0))
                                    .otherwise(0.0)
                    );

                    Expression<Double> viewWeighted = cb.prod(viewExpr, 0.1);
                    Expression<Double> revenueWeighted = cb.prod(revenueExpr, 0.6);
                    Expression<Double> deliveredWeighted = cb.prod(deliveredExpr, 0.3);

                    sortExpr = cb.sum(cb.sum(viewWeighted, revenueWeighted), deliveredWeighted);
                    query.groupBy(root.get("id"));
                }
                default -> sortExpr = root.get("createdDate");
            }

            log.info("Applying sort direction to query");
            Objects.requireNonNull(query).orderBy(
                    filter.getSortDirection() == SortDirection.ASC
                            ? cb.asc(sortExpr)
                            : cb.desc(sortExpr)
            );

            log.info("LibrarySpecification query built successfully");
            return query.getRestriction();
        };
    }
}

