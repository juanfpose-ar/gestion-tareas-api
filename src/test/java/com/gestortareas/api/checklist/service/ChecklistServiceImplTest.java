package com.gestortareas.api.checklist.service;

import com.gestortareas.api.checklist.dto.ChecklistItemDTO;
import com.gestortareas.api.checklist.entity.ChecklistItem;
import com.gestortareas.api.checklist.repository.ChecklistItemRepository;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import com.gestortareas.api.ticket.entity.Ticket;
import com.gestortareas.api.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChecklistServiceImplTest {

    @Mock
    private ChecklistItemRepository itemRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private ChecklistServiceImpl checklistService;

    private Ticket ticket;
    private ChecklistItem item;

    @BeforeEach
    public void setup() {
        ticket = Ticket.builder().id(1L).titulo("Ticket Test").build();
        item = new ChecklistItem();
        item.setId(10L);
        item.setTicket(ticket);
        item.setTexto("Checklist Item 1");
        item.setCompletado(false);
        item.setOrden((short) 1);
    }

    @Test
    public void testFindByTicketId() {
        when(itemRepository.findByTicketIdOrderByOrden(1L)).thenReturn(List.of(item));

        List<ChecklistItemDTO> result = checklistService.findByTicketId(1L);

        assertEquals(1, result.size());
        assertEquals("Checklist Item 1", result.get(0).getTexto());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    public void testAddItem_Exitoso() {
        ChecklistItemDTO request = ChecklistItemDTO.builder().texto("New Item").orden((short) 2).completado(false)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(itemRepository.save(any(ChecklistItem.class))).thenAnswer(inv -> {
            ChecklistItem cli = inv.getArgument(0);
            cli.setId(11L);
            return cli;
        });

        ChecklistItemDTO result = checklistService.addItem(1L, request);

        assertNotNull(result);
        assertEquals(11L, result.getId());
        assertEquals("New Item", result.getTexto());
        assertEquals((short) 2, result.getOrden());
    }

    @Test
    public void testAddItem_TicketNoEncontrado() {
        ChecklistItemDTO request = ChecklistItemDTO.builder().texto("New Item").build();
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> checklistService.addItem(1L, request));
    }

    @Test
    public void testUpdateItem_Exitoso() {
        ChecklistItemDTO request = ChecklistItemDTO.builder().texto("Updated Item").orden((short) 3).completado(true)
                .build();

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        ChecklistItemDTO result = checklistService.updateItem(10L, request);

        assertNotNull(result);
        assertEquals("Updated Item", result.getTexto());
        assertEquals((short) 3, result.getOrden());
        assertTrue(result.getCompletado());
    }

    @Test
    public void testUpdateItem_NoEncontrado() {
        ChecklistItemDTO request = ChecklistItemDTO.builder().texto("Updated").build();
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> checklistService.updateItem(10L, request));
    }

    @Test
    public void testDeleteItem_Exitoso() {
        when(itemRepository.existsById(10L)).thenReturn(true);

        checklistService.deleteItem(10L);

        verify(itemRepository, times(1)).deleteById(10L);
    }

    @Test
    public void testDeleteItem_NoEncontrado() {
        when(itemRepository.existsById(10L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> checklistService.deleteItem(10L));
    }

    @Test
    public void testToggleItem_Exitoso() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        ChecklistItemDTO result = checklistService.toggleItem(10L);

        assertNotNull(result);
        assertTrue(result.getCompletado());
    }

    @Test
    public void testReordenar() {
        ChecklistItem item2 = new ChecklistItem();
        item2.setId(20L);
        item2.setTicket(ticket);
        item2.setOrden((short) 2);

        when(itemRepository.findByTicketIdOrderByOrden(1L)).thenReturn(List.of(item, item2));

        checklistService.reordenar(1L, List.of(20L, 10L));

        assertEquals((short) 1, item.getOrden());
        assertEquals((short) 0, item2.getOrden());
    }
}
