package com.whereismymoney.backend.service;

import com.whereismymoney.backend.dto.PortfolioAnalyticsResponse;
import com.whereismymoney.backend.entity.Holding;
import com.whereismymoney.backend.repository.HoldingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class PortfolioAnalyticsServiceImpl
        implements PortfolioAnalyticsService {

    private final HoldingRepository holdingRepository;
    private final OwnershipService ownershipService;

    public PortfolioAnalyticsServiceImpl(
            HoldingRepository holdingRepository,
            OwnershipService ownershipService) {
        this.holdingRepository = holdingRepository;
        this.ownershipService = ownershipService;
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioAnalyticsResponse getAnalytics(
            Long portfolioId,
            Long userId) {

        ownershipService.getOwnedPortfolio(
                portfolioId,
                userId);

        List<Holding> holdings = holdingRepository
                .findByPortfolioIdAndPortfolioUserId(
                        portfolioId,
                        userId);

        BigDecimal totalCost = holdings.stream()
                .map(h -> h.getQuantity()
                        .multiply(
                                h.getAverageBuyPrice()))
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        BigDecimal totalMarketValue = holdings.stream()
                .map(h -> h.getQuantity()
                        .multiply(
                                h.getCurrentPrice() == null
                                        ? BigDecimal.ZERO
                                        : h.getCurrentPrice()))
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        BigDecimal unrealizedPnl = totalMarketValue.subtract(totalCost);

        BigDecimal unrealizedPnlPercent = totalCost.signum() == 0
                ? BigDecimal.ZERO
                : unrealizedPnl
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                totalCost,
                                4,
                                RoundingMode.HALF_UP);

        BigDecimal largestHolding = holdings.stream()
                .map(h -> h.getQuantity()
                        .multiply(
                                h.getCurrentPrice() == null
                                        ? BigDecimal.ZERO
                                        : h.getCurrentPrice()))
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        BigDecimal concentrationPercent = totalMarketValue.signum() == 0
                ? BigDecimal.ZERO
                : largestHolding
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                totalMarketValue,
                                4,
                                RoundingMode.HALF_UP);

        List<PortfolioAnalyticsResponse.Allocation> allocations = holdings.stream()
                .map(h -> {

                    BigDecimal marketValue = h.getQuantity()
                            .multiply(
                                    h.getCurrentPrice() == null
                                            ? BigDecimal.ZERO
                                            : h.getCurrentPrice());

                    BigDecimal allocationPercent = totalMarketValue.signum() == 0
                            ? BigDecimal.ZERO
                            : marketValue
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(
                                            totalMarketValue,
                                            4,
                                            RoundingMode.HALF_UP);

                    return new PortfolioAnalyticsResponse.Allocation(
                            h.getAsset().getId(),
                            h.getAsset().getSymbol(),
                            h.getAsset().getAssetType(),
                            marketValue,
                            allocationPercent);
                })
                .toList();

        return new PortfolioAnalyticsResponse(
                totalCost,
                totalMarketValue,
                unrealizedPnl,
                unrealizedPnlPercent,
                concentrationPercent,
                allocations);
    }
}