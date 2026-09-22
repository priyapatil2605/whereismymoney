package com.whereismymoney.backend.service;

import com.whereismymoney.backend.dto.HoldingRequest;
import com.whereismymoney.backend.dto.HoldingResponse;
import com.whereismymoney.backend.entity.Asset;
import com.whereismymoney.backend.entity.Holding;
import com.whereismymoney.backend.entity.Portfolio;
import com.whereismymoney.backend.repository.AssetRepository;
import com.whereismymoney.backend.repository.HoldingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HoldingServiceImpl implements HoldingService {

    private final HoldingRepository holdingRepository;
    private final AssetRepository assetRepository;
    private final OwnershipService ownershipService;

    public HoldingServiceImpl(
            HoldingRepository holdingRepository,
            AssetRepository assetRepository,
            OwnershipService ownershipService) {
        this.holdingRepository = holdingRepository;
        this.assetRepository = assetRepository;
        this.ownershipService = ownershipService;
    }

    @Override
    @Transactional
    public HoldingResponse upsert(
            Long portfolioId,
            HoldingRequest request,
            Long userId) {
        Portfolio portfolio = ownershipService.getOwnedPortfolio(portfolioId, userId);

        Asset asset = assetRepository
                .findById(request.assetId())
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        Holding holding = holdingRepository
                .findByPortfolioIdAndAssetIdAndPortfolioUserId(
                        portfolioId,
                        request.assetId(),
                        userId)
                .orElseGet(Holding::new);

        holding.setPortfolio(portfolio);
        holding.setAsset(asset);
        holding.setQuantity(request.quantity());
        holding.setAverageBuyPrice(request.averageBuyPrice());

        if (request.currentPrice() != null) {
            holding.setCurrentPrice(request.currentPrice());
            holding.setLastPriceUpdatedAt(LocalDateTime.now());
        }

        return toResponse(holdingRepository.save(holding));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoldingResponse> getAll(
            Long portfolioId,
            Long userId) {
        ownershipService.getOwnedPortfolio(portfolioId, userId);

        return holdingRepository
                .findByPortfolioIdAndPortfolioUserId(
                        portfolioId,
                        userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HoldingResponse get(
            Long holdingId,
            Long userId) {
        return holdingRepository
                .findByIdAndPortfolioUserId(
                        holdingId,
                        userId)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Holding not found"));
    }

    @Override
    @Transactional
    public void delete(
            Long holdingId,
            Long userId) {
        Holding holding = holdingRepository
                .findByIdAndPortfolioUserId(
                        holdingId,
                        userId)
                .orElseThrow(() -> new RuntimeException("Holding not found"));

        holdingRepository.delete(holding);
    }

    private HoldingResponse toResponse(Holding holding) {

        BigDecimal currentPrice = holding.getCurrentPrice() == null
                ? BigDecimal.ZERO
                : holding.getCurrentPrice();

        BigDecimal costBasis = holding.getQuantity()
                .multiply(holding.getAverageBuyPrice());

        BigDecimal marketValue = holding.getQuantity()
                .multiply(currentPrice);

        BigDecimal pnl = marketValue.subtract(costBasis);

        BigDecimal pnlPercent = costBasis.signum() == 0
                ? BigDecimal.ZERO
                : pnl
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                costBasis,
                                4,
                                RoundingMode.HALF_UP);

        return new HoldingResponse(
                holding.getId(),
                holding.getAsset().getId(),
                holding.getAsset().getSymbol(),
                holding.getAsset().getName(),
                holding.getAsset().getAssetType(),
                holding.getQuantity(),
                holding.getAverageBuyPrice(),
                holding.getCurrentPrice(),
                marketValue,
                costBasis,
                pnl,
                pnlPercent);
    }
}