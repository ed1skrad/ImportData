package com.old.system.notes.controller;

import com.old.system.notes.model.Client;
import com.old.system.notes.model.Notes;
import com.old.system.notes.service.ClientService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/clients")
    public List<Client> getAllClients() {
        return clientService.getAllClients();
    }

    @GetMapping("/notes")
    public List<Notes> getNotesByClientGuidAndAgencyAndCreatedDateTimeBetween(
            @RequestParam String clientGuid,
            @RequestParam String agency,
            @RequestParam LocalDateTime dateFrom,
            @RequestParam LocalDateTime dateTo) {
        return clientService.getNotesByClientGuidAndAgencyAndCreatedDateTimeBetween(clientGuid, agency, dateFrom, dateTo);
    }

    @PostMapping("/clients")
    public Client createClient(@RequestBody Client client) {
        return clientService.createClient(client);
    }

    @PostMapping("/clients/{clientGuid}/notes")
    public Notes createNoteForClient(@PathVariable String clientGuid, @RequestBody Notes note) {
        return clientService.createNoteForClient(clientGuid, note);
    }

    @PutMapping("/clients/{guid}")
    public Client updateClient(@PathVariable String guid, @RequestBody Client client) {
        return clientService.updateClient(guid, client);
    }

    @DeleteMapping("/clients/{guid}")
    public void deleteClient(@PathVariable String guid) {
        clientService.deleteClient(guid);
    }

    @PutMapping("/notes/{guid}")
    public Notes updateNote(@PathVariable String guid, @RequestBody Notes note) {
        return clientService.updateNote(guid, note);
    }

    @DeleteMapping("/notes/{guid}")
    public void deleteNote(@PathVariable String guid) {
        clientService.deleteNote(guid);
    }
}
