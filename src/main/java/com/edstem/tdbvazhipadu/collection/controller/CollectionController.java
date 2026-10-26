package com.edstem.tdbvazhipadu.collection.controller;

import com.edstem.tdbvazhipadu.collection.dto.DailyCollectionResponse;
import com.edstem.tdbvazhipadu.collection.service.CollectionService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping("/daily")
    public DailyCollectionResponse daily(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return collectionService.daily(date);
    }
}
