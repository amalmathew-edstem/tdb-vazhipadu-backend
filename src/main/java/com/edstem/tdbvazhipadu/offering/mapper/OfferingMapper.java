package com.edstem.tdbvazhipadu.offering.mapper;

import com.edstem.tdbvazhipadu.offering.dto.OfferingResponse;
import com.edstem.tdbvazhipadu.offering.entity.Offering;
import org.springframework.stereotype.Component;

@Component
public class OfferingMapper {

    public OfferingResponse toResponse(Offering offering) {
        return new OfferingResponse(offering.getId(), offering.getType(), offering.getPrice(), offering.isActive());
    }
}
