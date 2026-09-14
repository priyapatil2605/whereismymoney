package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.SupportTicket;
import com.whereismymoney.backend.entity.TicketPriority;
import com.whereismymoney.backend.entity.TicketStatus;

import java.util.List;

public interface SupportTicketService {

    SupportTicket createTicket(
            SupportTicket ticket,
            Long userId);

    List<SupportTicket> getMyTickets(
            Long userId);

    SupportTicket getTicket(
            Long ticketId,
            Long userId);

    SupportTicket updateStatus(
            Long ticketId,
            TicketStatus status,
            Long userId);

    SupportTicket updatePriority(
            Long ticketId,
            TicketPriority priority,
            Long userId);

    SupportTicket resolveTicket(
            Long ticketId,
            String resolution,
            Long userId);
}