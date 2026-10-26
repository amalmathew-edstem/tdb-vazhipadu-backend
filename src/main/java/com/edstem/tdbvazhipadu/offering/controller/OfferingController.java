package com.edstem.tdbvazhipadu.offering.controller;

import com.edstem.tdbvazhipadu.offering.dto.OfferingRequest;
import com.edstem.tdbvazhipadu.offering.dto.OfferingResponse;
import com.edstem.tdbvazhipadu.offering.service.OfferingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/offerings")
@RequiredArgsConstructor
public class OfferingController {

    private final OfferingService offeringService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferingResponse create(@Valid @RequestBody OfferingRequest request) {
        return offeringService.create(request);
    }

    @PutMapping("/{id}")
    public OfferingResponse update(@PathVariable Long id, @Valid @RequestBody OfferingRequest request) {
        return offeringService.update(id, request);
    }

    @GetMapping("/{id}")
    public OfferingResponse get(@PathVariable Long id) {
        return offeringService.get(id);
    }

    @GetMapping
    public Page<OfferingResponse> list(@PageableDefault(sort = "name") Pageable pageable) {
        return offeringService.list(pageable);
    }
}
