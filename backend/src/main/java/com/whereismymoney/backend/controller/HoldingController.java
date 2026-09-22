package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.dto.HoldingRequest;
import com.whereismymoney.backend.dto.HoldingResponse;
import com.whereismymoney.backend.service.HoldingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/holdings")
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @GetMapping
    public List<HoldingResponse> getAll(
            @PathVariable Long portfolioId,
            Authentication authentication) {
        return holdingService.getAll(
                portfolioId,
                getUserId(authentication));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HoldingResponse upsert(
            @PathVariable Long portfolioId,
            @Valid @RequestBody HoldingRequest request,
            Authentication authentication) {
        return holdingService.upsert(
                portfolioId,
                request,
                getUserId(authentication));
    }

    @GetMapping("/{holdingId}")
    public HoldingResponse get(
            @PathVariable Long holdingId,
            Authentication authentication) {
        return holdingService.get(
                holdingId,
                getUserId(authentication));
    }

    @DeleteMapping("/{holdingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long holdingId,
            Authentication authentication) {
        holdingService.delete(
                holdingId,
                getUserId(authentication));
    }

    private Long getUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}