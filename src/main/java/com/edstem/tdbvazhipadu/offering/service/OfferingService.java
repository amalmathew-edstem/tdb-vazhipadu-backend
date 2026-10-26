package com.edstem.tdbvazhipadu.offering.service;

import com.edstem.tdbvazhipadu.common.exception.ResourceNotFoundException;
import com.edstem.tdbvazhipadu.offering.dto.OfferingRequest;
import com.edstem.tdbvazhipadu.offering.dto.OfferingResponse;
import com.edstem.tdbvazhipadu.offering.entity.Offering;
import com.edstem.tdbvazhipadu.offering.mapper.OfferingMapper;
import com.edstem.tdbvazhipadu.offering.repository.OfferingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OfferingService {

    private final OfferingRepository offeringRepository;
    private final OfferingMapper offeringMapper;

    @Transactional
    public OfferingResponse create(OfferingRequest request) {
        Offering offering = new Offering(request.type(), request.price(), request.active(), request.dailyLimit());
        return offeringMapper.toResponse(offeringRepository.save(offering));
    }

    @Transactional
    public OfferingResponse update(Long id, OfferingRequest request) {
        Offering offering = findOffering(id);
        offering.revise(request.type(), request.price(), request.active(), request.dailyLimit());
        return offeringMapper.toResponse(offering);
    }

    @Transactional(readOnly = true)
    public OfferingResponse get(Long id) {
        return offeringMapper.toResponse(findOffering(id));
    }

    @Transactional(readOnly = true)
    public Page<OfferingResponse> list(Pageable pageable) {
        return offeringRepository.findAll(pageable).map(offeringMapper::toResponse);
    }

    private Offering findOffering(Long id) {
        return offeringRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Offering", id));
    }
}
