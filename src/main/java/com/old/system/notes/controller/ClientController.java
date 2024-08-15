package com.old.system.notes.controller;

import com.old.system.notes.model.dto.ClientDto;
import com.old.system.notes.model.dto.NotesDto;
import com.old.system.notes.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping("/clients")
    public List<ClientDto> getAllClients() {
        return clientService.getAllClients();
    }

    @GetMapping("/notes")
    public List<NotesDto> getNotesByClientGuidAndAgencyAndCreatedDateTimeBetween(
            @RequestParam String clientGuid,
            @RequestParam String agency,
            @RequestParam LocalDateTime dateFrom,
            @RequestParam LocalDateTime dateTo) {
        return clientService.getNotesByClientGuidAndAgencyAndCreatedDateTimeBetween(clientGuid, agency, dateFrom, dateTo);
    }
}
