package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.SupportTicket;
import com.whereismymoney.backend.entity.TicketPriority;
import com.whereismymoney.backend.entity.TicketStatus;
import com.whereismymoney.backend.entity.User;
import com.whereismymoney.backend.repository.SupportTicketRepository;
import com.whereismymoney.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportTicketServiceImpl
        implements SupportTicketService {

    private static final Logger logger = LoggerFactory.getLogger(
            SupportTicketServiceImpl.class);

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;

    public SupportTicketServiceImpl(
            SupportTicketRepository supportTicketRepository,
            UserRepository userRepository) {

        this.supportTicketRepository = supportTicketRepository;

        this.userRepository = userRepository;
    }

    @Override
    public SupportTicket createTicket(
            SupportTicket ticket,
            Long userId) {

        if (ticket.getIssueTitle() == null ||
                ticket.getIssueTitle().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Issue title is required");
        }

        if (ticket.getDescription() == null ||
                ticket.getDescription().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Description is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));

        ticket.setUser(user);
        ticket.setStatus(TicketStatus.OPEN);

        if (ticket.getPriority() == null) {
            ticket.setPriority(TicketPriority.MEDIUM);
        }

        ticket.setResolution(null);
        ticket.setResolvedAt(null);

        SupportTicket saved = supportTicketRepository.save(ticket);

        logger.info(
                "Support ticket created: ticketId={}, userId={}",
                saved.getId(),
                userId);

        return saved;
    }

    @Override
    public List<SupportTicket> getMyTickets(
            Long userId) {

        return supportTicketRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public SupportTicket getTicket(
            Long ticketId,
            Long userId) {

        return supportTicketRepository
                .findByIdAndUserId(ticketId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You do not have access to this support ticket"));
    }

    @Override
    public SupportTicket updateStatus(
            Long ticketId,
            TicketStatus status,
            Long userId) {

        if (status == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status is required");
        }

        SupportTicket ticket = getTicket(ticketId, userId);

        ticket.setStatus(status);

        if (status == TicketStatus.RESOLVED) {

            if (ticket.getResolution() == null ||
                    ticket.getResolution().isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Resolution notes are required");
            }

            ticket.setResolvedAt(
                    LocalDateTime.now());

        } else {
            ticket.setResolvedAt(null);
        }

        return supportTicketRepository.save(ticket);
    }

    @Override
    public SupportTicket updatePriority(
            Long ticketId,
            TicketPriority priority,
            Long userId) {

        if (priority == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Priority is required");
        }

        SupportTicket ticket = getTicket(ticketId, userId);

        ticket.setPriority(priority);

        return supportTicketRepository.save(ticket);
    }

    @Override
    public SupportTicket resolveTicket(
            Long ticketId,
            String resolution,
            Long userId) {

        if (resolution == null ||
                resolution.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Resolution notes are required");
        }

        SupportTicket ticket = getTicket(ticketId, userId);

        ticket.setResolution(resolution);
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());

        SupportTicket saved = supportTicketRepository.save(ticket);

        logger.info(
                "Support ticket resolved: ticketId={}, userId={}",
                ticketId,
                userId);

        return saved;
    }
}