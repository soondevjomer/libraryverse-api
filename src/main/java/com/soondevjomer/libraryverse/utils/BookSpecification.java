package com.soondevjomer.libraryverse.utils;

import com.soondevjomer.libraryverse.constant.SortBy;
import com.soondevjomer.libraryverse.constant.SortDirection;
import com.soondevjomer.libraryverse.dto.FilterDto;
import com.soondevjomer.libraryverse.model.Book;
import com.soondevjomer.libraryverse.model.BookDetail;
import com.soondevjomer.libraryverse.model.Inventory;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.soondevjomer.libraryverse.constant.SortBy.*;

@Slf4j
public class BookSpecification {

    public static Specification<Book> filterBooks(FilterDto filter) {
        return (root, query, cb) -> {
            Join<Book, BookDetail> detailJoin = root.join("bookDetail", JoinType.LEFT);
            Join<Book, Inventory> inventoryJoin = root.join("inventory", JoinType.LEFT);

            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            log.info("Applying search filter");
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(detailJoin.get("title")),
                                "%" + filter.getSearch().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getLibraryId() != null && filter.getLibraryId() != 0) {
                log.info("Applying library filter with ID: {}", filter.getLibraryId());
                predicates.add(
                        cb.equal(root.get("library").get("id"), filter.getLibraryId())
                );
            }

            if (!predicates.isEmpty()) {
                query.where(cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            log.info("Applying a sort by and sort direction or default");
            if (filter.getSortBy() == null) filter.setSortBy(SortBy.CREATED_DATE);
            if (filter.getSortDirection() == null) filter.setSortDirection(SortDirection.ASC);

            Expression<?> sortExpr;

            log.info("Applying dynamic sort for: {}", filter.getSortBy());
            switch (filter.getSortBy()) {
                case CREATED_DATE -> sortExpr = root.get("createdDate");
                case TITLE -> sortExpr = detailJoin.get("title");
                case PRICE -> sortExpr = detailJoin.get("price");
                case STOCK -> sortExpr = inventoryJoin.get("availableStock");
                case SOLD -> sortExpr = inventoryJoin.get("delivered");
                case POPULAR -> {
                    log.info("calculating the sum of view count * .2 and sale/delivered * .8");
                    Expression<Double> deliveredExpr = cb.toDouble(inventoryJoin.get("delivered"));
                    Expression<Double> deliveredNonNull = cb.coalesce(deliveredExpr, 0.0);

                    Expression<Double> viewExpr = cb.toDouble(root.get("viewCount"));
                    Expression<Double> viewNonNull = cb.coalesce(viewExpr, 0.0);

                    Expression<Double> viewsWeighted = cb.prod(viewNonNull, 0.2);
                    Expression<Double> salesWeighted = cb.prod(deliveredNonNull, 0.8);

                    sortExpr = cb.sum(viewsWeighted, salesWeighted);
                }
                default -> sortExpr = root.get("createdDate");
            }

            log.info("Applying sort direction to query");
            Objects.requireNonNull(query).orderBy(
                    filter.getSortDirection() == SortDirection.ASC
                            ? cb.asc(sortExpr)
                            : cb.desc(sortExpr)
            );

            log.info("Applying the query created to query");
            return query.getRestriction();
        };
    }

}
