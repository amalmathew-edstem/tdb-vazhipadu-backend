package com.edstem.tdbvazhipadu.collection.repository;

import com.edstem.tdbvazhipadu.collection.dto.OfferingCollection;
import com.edstem.tdbvazhipadu.order.entity.OrderItem;
import com.edstem.tdbvazhipadu.order.entity.OrderStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface CollectionRepository extends Repository<OrderItem, Long> {

    @Query(
            """
            select new com.edstem.tdbvazhipadu.collection.dto.OfferingCollection(
                offering.id, offering.type, sum(item.quantity), sum(item.lineTotal))
            from OrderItem item
            join item.order placed
            join item.offering offering
            where placed.createdAt >= :from
              and placed.createdAt < :to
              and placed.status <> :excludedStatus
            group by offering.id, offering.type
            order by offering.type, offering.id
            """)
    List<OfferingCollection> collectByOffering(
            @Param("from") Instant from, @Param("to") Instant to, @Param("excludedStatus") OrderStatus excludedStatus);
}
