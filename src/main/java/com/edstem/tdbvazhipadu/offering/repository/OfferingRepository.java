package com.edstem.tdbvazhipadu.offering.repository;

import com.edstem.tdbvazhipadu.offering.entity.Offering;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfferingRepository extends JpaRepository<Offering, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Offering o where o.id in :ids and o.dailyLimit is not null order by o.id")
    List<Offering> lockDailyLimitedByIdIn(@Param("ids") Collection<Long> ids);
}
