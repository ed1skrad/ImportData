package com.notes.scheduled.service;

import com.notes.scheduled.model.CompanyUser;
import com.notes.scheduled.model.PatientNote;
import com.notes.scheduled.model.PatientProfile;
import com.notes.scheduled.model.old.Client;
import com.notes.scheduled.model.old.Notes;
import com.notes.scheduled.repository.CompanyUserRepository;
import com.notes.scheduled.repository.PatientNoteRepository;
import com.notes.scheduled.repository.PatientProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;

@Service
public class ImportService {

    private static final Logger logger = LoggerFactory.getLogger(ImportService.class);

    private final CompanyUserRepository companyUserRepository;

    private final PatientProfileRepository patientProfileRepository;

    private final PatientNoteRepository patientNoteRepository;

    private final RestTemplate restTemplate;

    public ImportService(CompanyUserRepository companyUserRepository, PatientProfileRepository patientProfileRepository, PatientNoteRepository patientNoteRepository, RestTemplate restTemplate) {
        this.companyUserRepository = companyUserRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.patientNoteRepository = patientNoteRepository;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 10000)
    public void importData() {
        logger.info("Starting data import from old system to new system");

        try {
            Client[] clients = restTemplate.getForObject("http://localhost:8080/clients", Client[].class);

            if (clients != null) {
                for (Client client : clients) {
                    if ("Active".equalsIgnoreCase(client.getStatus())) {
                        importClient(client);
                    } else {
                        logger.info("Skipping client with guid: {} because status is not Active", client.getGuid());
                    }
                }
            }

            logger.info("Data import completed successfully");
        } catch (Exception e) {
            logger.error("Error during data import", e);
        }
    }

    private void importClient(Client client) {
        Optional<PatientProfile> patientProfileOptional = patientProfileRepository.findByOldClientGuidContaining(client.getGuid());
        PatientProfile patientProfile;
        patientProfile = patientProfileOptional.orElseGet(() -> createPatientProfile(client));

        if (client.getNotes() != null) {
            for (Notes note : client.getNotes()) {
                importNote(note, patientProfile);
            }
        }
    }

    private PatientProfile createPatientProfile(Client client) {
        PatientProfile patientProfile = new PatientProfile();
        patientProfile.setFirstName(client.getFirstName());
        patientProfile.setLastName(client.getLastName());
        patientProfile.setOldClientGuid(client.getGuid());
        patientProfile.setStatusId(getStatusId(client.getStatus()));
        return patientProfileRepository.save(patientProfile);
    }

    private Short getStatusId(String status) {
        return switch (status) {
            case "ACTIVE" -> 200;
            case "INACTIVE" -> 210;
            case "PENDING" -> 230;
            default -> 0;
        };
    }

    private void importNote(Notes note, PatientProfile patientProfile) {
        Optional<CompanyUser> companyUserOptional = companyUserRepository.findByLogin(note.getLoggedUser());
        CompanyUser companyUser;
        companyUser = companyUserOptional.orElseGet(() -> createCompanyUser(note.getLoggedUser()));

        Optional<PatientNote> existingNoteOptional = patientNoteRepository.findByNoteAndPatientId(note.getComments(), patientProfile.getId());
        PatientNote patientNote;
        if (existingNoteOptional.isPresent()) {
            patientNote = existingNoteOptional.get();
            updatePatientNote(patientNote, note, companyUser);
        } else {
            patientNote = createPatientNote(note, patientProfile, companyUser);
        }

        patientNoteRepository.save(patientNote);
        logger.info("Imported note with guid: {}", note.getGuid());
    }

    private CompanyUser createCompanyUser(String login) {
        CompanyUser companyUser = new CompanyUser();
        companyUser.setLogin(login);
        return companyUserRepository.save(companyUser);
    }

    private PatientNote createPatientNote(Notes note, PatientProfile patientProfile, CompanyUser companyUser) {
        PatientNote patientNote = new PatientNote();
        patientNote.setNote(note.getComments());
        patientNote.setCreatedDateTime(note.getCreatedDateTime());
        patientNote.setLastModifiedDateTime(note.getModifiedDateTime());
        patientNote.setCreatedByUser(companyUser);
        patientNote.setLastModifiedByUser(companyUser);
        patientNote.setPatient(patientProfile);
        return patientNote;
    }

    private void updatePatientNote(PatientNote patientNote, Notes note, CompanyUser companyUser) {
        if (note.getModifiedDateTime().isAfter(patientNote.getLastModifiedDateTime())) {
            patientNote.setNote(note.getComments());
            patientNote.setLastModifiedDateTime(note.getModifiedDateTime());
            patientNote.setLastModifiedByUser(companyUser);
        }
    }
}

