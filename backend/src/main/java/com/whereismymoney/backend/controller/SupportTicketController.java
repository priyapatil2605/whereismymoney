package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.entity.SupportTicket;
import com.whereismymoney.backend.entity.TicketPriority;
import com.whereismymoney.backend.entity.TicketStatus;
import com.whereismymoney.backend.entity.User;
import com.whereismymoney.backend.repository.SupportTicketRepository;
import com.whereismymoney.backend.repository.UserRepository;
import com.whereismymoney.backend.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/support-tickets")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;
    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;

    private final String adminEmail;

    public SupportTicketController(
            SupportTicketService supportTicketService,
            SupportTicketRepository supportTicketRepository,
            UserRepository userRepository,
            @Value("${admin.email}") String adminEmail) {

        this.supportTicketService = supportTicketService;

        this.supportTicketRepository = supportTicketRepository;

        this.userRepository = userRepository;

        this.adminEmail = adminEmail;
    }

    @PostMapping
    public ResponseEntity<SupportTicket> createTicket(
            @RequestBody SupportTicket ticket,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        SupportTicket createdTicket = supportTicketService.createTicket(
                ticket,
                userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdTicket);
    }

    @GetMapping
    public ResponseEntity<List<SupportTicket>> getMyTickets(
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                supportTicketService.getMyTickets(userId));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<SupportTicket> getTicket(
            @PathVariable Long ticketId,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                supportTicketService.getTicket(
                        ticketId,
                        userId));
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<SupportTicket> updateStatus(
            @PathVariable Long ticketId,
            @RequestParam TicketStatus status,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                supportTicketService.updateStatus(
                        ticketId,
                        status,
                        userId));
    }

    @PatchMapping("/{ticketId}/priority")
    public ResponseEntity<SupportTicket> updatePriority(
            @PathVariable Long ticketId,
            @RequestParam TicketPriority priority,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                supportTicketService.updatePriority(
                        ticketId,
                        priority,
                        userId));
    }

    @PatchMapping("/{ticketId}/resolve")
    public ResponseEntity<SupportTicket> resolveTicket(
            @PathVariable Long ticketId,
            @RequestParam String resolution,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                supportTicketService.resolveTicket(
                        ticketId,
                        resolution,
                        userId));
    }

    @GetMapping("/admin/open")
    public ResponseEntity<List<SupportTicket>> getOpenTicketsForAdmin(
            Authentication authentication) {

        Long userId = getUserId(authentication);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"));

        if (!adminEmail.equalsIgnoreCase(user.getEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin access required");
        }

        return ResponseEntity.ok(
                supportTicketRepository
                        .findByStatusOrderByCreatedAtDesc(
                                TicketStatus.OPEN));
    }

    private Long getUserId(
            Authentication authentication) {

        return (Long) authentication.getPrincipal();
    }
}