package com.whereismymoney.backend.repository;

import com.whereismymoney.backend.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

    List<Holding> findByPortfolioIdAndPortfolioUserId(
            Long portfolioId,
            Long userId);

    Optional<Holding> findByIdAndPortfolioUserId(
            Long holdingId,
            Long userId);

    Optional<Holding> findByPortfolioIdAndAssetIdAndPortfolioUserId(
            Long portfolioId,
            Long assetId,
            Long userId);
}