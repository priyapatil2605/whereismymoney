package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.entity.Portfolio;
import com.whereismymoney.backend.entity.User;
import com.whereismymoney.backend.repository.PortfolioRepository;
import com.whereismymoney.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    public PortfolioController(
            PortfolioRepository portfolioRepository,
            UserRepository userRepository) {

        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Portfolio> getMyPortfolios(
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return portfolioRepository.findByUserId(userId);
    }

    @GetMapping("/{id}")
    public Portfolio getPortfolio(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return portfolioRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AccessDeniedException(
                        "You do not have access to this portfolio"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Portfolio createPortfolio(
            @RequestBody Portfolio portfolio,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        portfolio.setUser(user);

        return portfolioRepository.save(portfolio);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePortfolio(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        Portfolio portfolio = portfolioRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AccessDeniedException(
                        "You do not have access to this portfolio"));

        portfolioRepository.delete(portfolio);
    }

    private Long getUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}