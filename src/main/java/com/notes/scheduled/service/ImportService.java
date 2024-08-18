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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                List<String> activeGuids = List.of(clients)
                        .stream()
                        .filter(client -> "ACTIVE".equalsIgnoreCase(client.getStatus()))
                        .map(Client::getGuid)
                        .collect(Collectors.toList());

                // Удаление неактивных профилей и связанных заметок
                List<PatientProfile> inactiveProfiles = patientProfileRepository.findAll()
                        .stream()
                        .filter(profile -> !activeGuids.contains(profile.getOldClientGuid()))
                        .collect(Collectors.toList());

                for (PatientProfile profile : inactiveProfiles) {
                    // Удаление связанных заметок
                    List<PatientNote> notes = patientNoteRepository.findByPatientId(profile.getId());
                    patientNoteRepository.deleteAll(notes);

                    // Удаление профиля пациента
                    patientProfileRepository.delete(profile);
                    logger.info("Deleted inactive patient profile with oldClientGuid: {}", profile.getOldClientGuid());
                }

                for (Client client : clients) {
                    if ("ACTIVE".equalsIgnoreCase(client.getStatus())) {
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

        if (patientProfileOptional.isPresent()) {
            patientProfile = patientProfileOptional.get();
            updatePatientProfile(patientProfile, client);
        } else {
            patientProfile = createPatientProfile(client);
        }

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

    private void updatePatientProfile(PatientProfile patientProfile, Client client) {
        patientProfile.setFirstName(client.getFirstName());
        patientProfile.setLastName(client.getLastName());
        patientProfile.setStatusId(getStatusId(client.getStatus()));
        patientProfileRepository.save(patientProfile);
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
            patientNoteRepository.save(patientNote);
            logger.info("Imported note with guid: {}", note.getGuid());
        }
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
            patientNoteRepository.save(patientNote);
            logger.info("Updated note with guid: {}", note.getGuid());
        }
    }
}
