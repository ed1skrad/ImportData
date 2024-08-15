package com.old.system.notes.service;

import com.old.system.notes.model.Client;
import com.old.system.notes.model.Notes;
import com.old.system.notes.model.dto.ClientDto;
import com.old.system.notes.model.dto.NotesDto;
import com.old.system.notes.repository.ClientRepository;
import com.old.system.notes.repository.NotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private NotesRepository notesRepository;

    @Transactional
    public List<ClientDto> getAllClients() {
        List<Client> clients = clientRepository.findAll();
        return clients.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<NotesDto> getNotesByClientGuidAndAgencyAndCreatedDateTimeBetween(
            String clientGuid, String agency, LocalDateTime dateFrom, LocalDateTime dateTo) {
        List<Notes> notes = notesRepository.findByClientGuidAndAgencyAndCreatedDateTimeBetween(clientGuid, agency, dateFrom, dateTo);
        return notes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ClientDto convertToDto(Client client) {
        ClientDto clientDto = new ClientDto();
        clientDto.setAgency(client.getAgency());
        clientDto.setGuid(client.getGuid());
        clientDto.setFirstName(client.getFirstName());
        clientDto.setLastName(client.getLastName());
        clientDto.setStatus(client.getStatus());
        clientDto.setDob(client.getDob());
        clientDto.setCreatedDateTime(client.getCreatedDateTime());
        return clientDto;
    }

    private NotesDto convertToDto(Notes notes) {
        NotesDto notesDto = new NotesDto();
        notesDto.setComments(notes.getComments());
        notesDto.setGuid(notes.getGuid());
        notesDto.setModifiedDateTime(notes.getModifiedDateTime());
        notesDto.setDateTime(notes.getDateTime());
        notesDto.setLoggedUser(notes.getLoggedUser());
        notesDto.setCreatedDateTime(notes.getCreatedDateTime());
        notesDto.setClientGuid(notes.getClient().getGuid());
        return notesDto;
    }
}
