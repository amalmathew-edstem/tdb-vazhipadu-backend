package com.edstem.tdbvazhipadu.order.repository;

import com.edstem.tdbvazhipadu.order.entity.VazhipaduOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<VazhipaduOrder, Long> {

    @EntityGraph(attributePaths = {"items", "items.offering"})
    Optional<VazhipaduOrder> findWithItemsById(Long id);
}
