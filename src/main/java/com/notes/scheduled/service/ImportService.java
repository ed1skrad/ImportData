package com.notes.scheduled.service;

import com.notes.scheduled.model.CompanyUser;
import com.notes.scheduled.model.PatientNote;
import com.notes.scheduled.model.PatientProfile;
import com.notes.scheduled.model.old.Client;
import com.notes.scheduled.model.old.Notes;
import com.notes.scheduled.repository.CompanyUserRepository;
import com.notes.scheduled.repository.PatientNoteRepository;
import com.notes.scheduled.repository.PatientProfileRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ImportService {

    private static final String ACTIVE_STATUS = "ACTIVE";

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

    @Scheduled(cron = "0 15 1/2 * * *")
    public void importData() {
        Client[] clients = restTemplate.getForObject("http://localhost:8080/clients", Client[].class);

        if (clients != null) {
            List<String> activeGuids = Stream.of(clients)
                    .filter(client -> ACTIVE_STATUS.equalsIgnoreCase(client.getStatus()))
                    .map(Client::getGuid)
                    .toList();

            List<PatientProfile> inactiveProfiles = patientProfileRepository.findAll()
                    .stream()
                    .filter(profile -> !activeGuids.contains(profile.getOldClientGuid()))
                    .toList();

            deleteInactiveProfiles(inactiveProfiles);

            for (Client client : clients) {
                if (ACTIVE_STATUS.equalsIgnoreCase(client.getStatus())) {
                    importClient(client);
                }
            }
        }
    }

    public void deleteInactiveProfiles(List<PatientProfile> inactiveProfiles) {
        for (PatientProfile profile : inactiveProfiles) {
            List<PatientNote> notes = patientNoteRepository.findByPatientId(profile.getId());
            patientNoteRepository.deleteAll(notes);
            patientProfileRepository.delete(profile);
        }
    }

    public void importClient(Client client) {
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

    public PatientProfile createPatientProfile(Client client) {
        PatientProfile patientProfile = new PatientProfile();
        patientProfile.setFirstName(client.getFirstName());
        patientProfile.setLastName(client.getLastName());
        patientProfile.setOldClientGuid(client.getGuid());
        patientProfile.setStatusId(getStatusId(client.getStatus()));
        return patientProfileRepository.save(patientProfile);
    }

    public void updatePatientProfile(PatientProfile patientProfile, Client client) {
        boolean isUpdated = false;

        if (!patientProfile.getFirstName().equals(client.getFirstName())) {
            patientProfile.setFirstName(client.getFirstName());
            isUpdated = true;
        }

        if (!patientProfile.getLastName().equals(client.getLastName())) {
            patientProfile.setLastName(client.getLastName());
            isUpdated = true;
        }

        Short newStatusId = getStatusId(client.getStatus());
        if (!patientProfile.getStatusId().equals(newStatusId)) {
            patientProfile.setStatusId(newStatusId);
            isUpdated = true;
        }

        if (isUpdated) {
            patientProfileRepository.save(patientProfile);
        }
    }

    public Short getStatusId(String status) {
        return switch (status) {
            case ACTIVE_STATUS -> 200;
            case "INACTIVE" -> 210;
            case "PENDING" -> 230;
            default -> 0;
        };
    }

    public void importNote(Notes note, PatientProfile patientProfile) {
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
        }
    }

    public CompanyUser createCompanyUser(String login) {
        CompanyUser companyUser = new CompanyUser();
        companyUser.setLogin(login);
        return companyUserRepository.save(companyUser);
    }

    public PatientNote createPatientNote(Notes note, PatientProfile patientProfile, CompanyUser companyUser) {
        PatientNote patientNote = new PatientNote();
        patientNote.setNote(note.getComments());
        patientNote.setCreatedDateTime(note.getCreatedDateTime());
        patientNote.setLastModifiedDateTime(note.getModifiedDateTime());
        patientNote.setCreatedByUser(companyUser);
        patientNote.setLastModifiedByUser(companyUser);
        patientNote.setPatient(patientProfile);
        return patientNote;
    }

    public void updatePatientNote(PatientNote patientNote, Notes note, CompanyUser companyUser) {
        if (note.getModifiedDateTime().isAfter(patientNote.getLastModifiedDateTime())) {
            patientNote.setNote(note.getComments());
            patientNote.setLastModifiedDateTime(note.getModifiedDateTime());
            patientNote.setLastModifiedByUser(companyUser);
            patientNoteRepository.save(patientNote);
        }
    }
}
