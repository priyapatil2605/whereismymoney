package com.whereismymoney.backend.service;

import com.whereismymoney.backend.dto.HoldingRequest;
import com.whereismymoney.backend.dto.HoldingResponse;

import java.util.List;

public interface HoldingService {

    HoldingResponse upsert(
            Long portfolioId,
            HoldingRequest request,
            Long userId);

    List<HoldingResponse> getAll(
            Long portfolioId,
            Long userId);

    HoldingResponse get(
            Long holdingId,
            Long userId);

    void delete(
            Long holdingId,
            Long userId);
}