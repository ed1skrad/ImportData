package com.old.system.notes.service;

import com.old.system.notes.model.Client;
import com.old.system.notes.model.Notes;
import com.old.system.notes.repository.ClientRepository;
import com.old.system.notes.repository.NotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    private final NotesRepository notesRepository;

    public ClientService(ClientRepository clientRepository, NotesRepository notesRepository) {
        this.clientRepository = clientRepository;
        this.notesRepository = notesRepository;
    }

    @Transactional
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public List<Notes> getNotesByClientGuidAndAgencyAndCreatedDateTimeBetween(
            String clientGuid, String agency, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return notesRepository.findByClientGuidAndAgencyAndCreatedDateTimeBetween(clientGuid, agency, dateFrom, dateTo);
    }

    public Client createClient(Client client) {
        return clientRepository.save(client);
    }

    public Notes createNoteForClient(String clientGuid, Notes note) {
        Client client = clientRepository.findById(clientGuid)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        note.setClient(client);
        return notesRepository.save(note);
    }

    public Client updateClient(String guid, Client clientDetails) {
        Client client = clientRepository.findById(guid).orElseThrow(() -> new RuntimeException("Client not found"));
        client.setAgency(clientDetails.getAgency());
        client.setFirstName(clientDetails.getFirstName());
        client.setLastName(clientDetails.getLastName());
        client.setStatus(clientDetails.getStatus());
        client.setDob(clientDetails.getDob());
        client.setCreatedDateTime(clientDetails.getCreatedDateTime());
        return clientRepository.save(client);
    }

    public void deleteClient(String guid) {
        clientRepository.deleteById(guid);
    }

    public Notes updateNote(String guid, Notes noteDetails) {
        Notes note = notesRepository.findById(guid).orElseThrow(() -> new RuntimeException("Note not found"));
        note.setComments(noteDetails.getComments());
        note.setModifiedDateTime(noteDetails.getModifiedDateTime());
        note.setDateTime(noteDetails.getDateTime());
        note.setLoggedUser(noteDetails.getLoggedUser());
        note.setCreatedDateTime(noteDetails.getCreatedDateTime());
        return notesRepository.save(note);
    }

    public void deleteNote(String guid) {
        notesRepository.deleteById(guid);
    }
}
