package com.edstem.tdbvazhipadu.order.repository;

import com.edstem.tdbvazhipadu.order.dto.OrderSearchCriteria;
import com.edstem.tdbvazhipadu.order.entity.VazhipaduOrder;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderSpecifications {

    public static Specification<VazhipaduOrder> matching(OrderSearchCriteria criteria) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.offeringDate() != null) {
                predicates.add(builder.equal(root.get("offeringDate"), criteria.offeringDate()));
            }
            if (criteria.status() != null) {
                predicates.add(builder.equal(root.get("status"), criteria.status()));
            }
            if (criteria.phoneNumber() != null) {
                predicates.add(builder.equal(root.get("phoneNumber"), criteria.phoneNumber()));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
