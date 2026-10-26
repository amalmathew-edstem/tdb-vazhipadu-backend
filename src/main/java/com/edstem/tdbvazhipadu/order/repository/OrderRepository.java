package com.edstem.tdbvazhipadu.order.repository;

import com.edstem.tdbvazhipadu.order.entity.OrderStatus;
import com.edstem.tdbvazhipadu.order.entity.VazhipaduOrder;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<VazhipaduOrder, Long>, JpaSpecificationExecutor<VazhipaduOrder> {

    @EntityGraph(attributePaths = {"items", "items.offering"})
    Optional<VazhipaduOrder> findWithItemsById(Long id);

    @Query(
            """
            select coalesce(sum(i.quantity), 0) from OrderItem i
            where i.offering.id = :offeringId
              and i.order.offeringDate = :offeringDate
              and i.order.status <> :excludedStatus
            """)
    long sumBookedQuantity(
            @Param("offeringId") Long offeringId,
            @Param("offeringDate") LocalDate offeringDate,
            @Param("excludedStatus") OrderStatus excludedStatus);
}
