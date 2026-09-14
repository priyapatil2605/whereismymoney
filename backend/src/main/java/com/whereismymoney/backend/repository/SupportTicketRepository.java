package com.whereismymoney.backend.repository;

import com.whereismymoney.backend.entity.SupportTicket;
import com.whereismymoney.backend.entity.TicketPriority;
import com.whereismymoney.backend.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository
        extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(
            Long userId);

    Optional<SupportTicket> findByIdAndUserId(
            Long ticketId,
            Long userId);

    List<SupportTicket> findByUserIdAndStatus(
            Long userId,
            TicketStatus status);

    List<SupportTicket> findByUserIdAndPriority(
            Long userId,
            TicketPriority priority);

    List<SupportTicket> findByStatusOrderByCreatedAtDesc(
            TicketStatus status);
}