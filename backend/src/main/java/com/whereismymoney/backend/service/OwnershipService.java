package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.Portfolio;
import com.whereismymoney.backend.repository.PortfolioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class OwnershipService {

    private final PortfolioRepository portfolioRepository;

    public OwnershipService(
            PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    public Portfolio getOwnedPortfolio(
            Long portfolioId,
            Long userId) {

        Portfolio portfolio = portfolioRepository
                .findById(portfolioId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Portfolio not found"));

        if (!portfolio.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You do not own this portfolio");
        }

        return portfolio;
    }
}