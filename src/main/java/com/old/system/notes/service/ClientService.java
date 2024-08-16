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

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private NotesRepository notesRepository;

    @Transactional
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public List<Notes> getNotesByClientGuidAndAgencyAndCreatedDateTimeBetween(
            String clientGuid, String agency, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return notesRepository.findByClientGuidAndAgencyAndCreatedDateTimeBetween(clientGuid, agency, dateFrom, dateTo);
    }
}
