package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.SupportTicket;
import com.whereismymoney.backend.entity.TicketPriority;
import com.whereismymoney.backend.entity.TicketStatus;
import com.whereismymoney.backend.entity.User;
import com.whereismymoney.backend.repository.SupportTicketRepository;
import com.whereismymoney.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceImplTest {

    @Mock
    private SupportTicketRepository supportTicketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SupportTicketServiceImpl supportTicketService;

    @Test
    void createTicketShouldAssignUserAndOpenStatus() {

        User user = new User();

        SupportTicket ticket = new SupportTicket();

        ticket.setIssueTitle("Test issue");
        ticket.setDescription("Test description");
        ticket.setPriority(TicketPriority.HIGH);

        when(userRepository.findById(11L))
                .thenReturn(Optional.of(user));

        when(supportTicketRepository.save(any(SupportTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SupportTicket result = supportTicketService.createTicket(
                ticket,
                11L);

        assertEquals(
                user,
                result.getUser());

        assertEquals(
                TicketStatus.OPEN,
                result.getStatus());

        assertEquals(
                TicketPriority.HIGH,
                result.getPriority());

        verify(supportTicketRepository)
                .save(ticket);
    }

    @Test
    void createTicketShouldRejectMissingTitle() {

        SupportTicket ticket = new SupportTicket();

        ticket.setDescription("Test description");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> supportTicketService.createTicket(
                        ticket,
                        11L));

        assertEquals(
                400,
                exception.getStatusCode().value());

        verify(supportTicketRepository, never())
                .save(any(SupportTicket.class));
    }

    @Test
    void createTicketShouldRejectMissingDescription() {

        SupportTicket ticket = new SupportTicket();

        ticket.setIssueTitle("Test issue");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> supportTicketService.createTicket(
                        ticket,
                        11L));

        assertEquals(
                400,
                exception.getStatusCode().value());

        verify(supportTicketRepository, never())
                .save(any(SupportTicket.class));
    }

    @Test
    void userCannotAccessAnotherUsersTicket() {

        when(supportTicketRepository
                .findByIdAndUserId(1L, 12L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> supportTicketService.getTicket(
                        1L,
                        12L));

        assertEquals(
                403,
                exception.getStatusCode().value());
    }

    @Test
    void ticketCanBeResolved() {

        SupportTicket ticket = new SupportTicket();

        ticket.setStatus(TicketStatus.IN_PROGRESS);

        when(supportTicketRepository
                .findByIdAndUserId(1L, 11L))
                .thenReturn(Optional.of(ticket));

        when(supportTicketRepository.save(any(SupportTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SupportTicket result = supportTicketService.resolveTicket(
                1L,
                "Issue resolved successfully.",
                11L);

        assertEquals(
                TicketStatus.RESOLVED,
                result.getStatus());

        assertEquals(
                "Issue resolved successfully.",
                result.getResolution());

        verify(supportTicketRepository)
                .save(ticket);
    }
}