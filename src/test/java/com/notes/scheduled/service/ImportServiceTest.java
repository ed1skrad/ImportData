package com.notes.scheduled.service;

import com.notes.scheduled.model.CompanyUser;
import com.notes.scheduled.model.PatientNote;
import com.notes.scheduled.model.PatientProfile;
import com.notes.scheduled.model.old.Client;
import com.notes.scheduled.model.old.Notes;
import com.notes.scheduled.repository.CompanyUserRepository;
import com.notes.scheduled.repository.PatientNoteRepository;
import com.notes.scheduled.repository.PatientProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ImportServiceTest {

    @Mock
    private CompanyUserRepository companyUserRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private PatientNoteRepository patientNoteRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ImportService importService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testImportData_NoClients() {
        when(restTemplate.getForObject(anyString(), eq(Client[].class))).thenReturn(null);

        importService.importData();

        verify(patientProfileRepository, never()).save(any(PatientProfile.class));
        verify(patientProfileRepository, never()).delete(any(PatientProfile.class));
        verify(patientNoteRepository, never()).save(any(PatientNote.class));
    }

    @Test
    void testImportData_WithActiveClient_NewProfile() {
        Client client = new Client();
        client.setGuid("123");
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setStatus("ACTIVE");

        when(restTemplate.getForObject(anyString(), eq(Client[].class))).thenReturn(new Client[]{client});
        when(patientProfileRepository.findByOldClientGuidContaining("123")).thenReturn(Optional.empty());

        importService.importData();

        ArgumentCaptor<PatientProfile> captor = ArgumentCaptor.forClass(PatientProfile.class);
        verify(patientProfileRepository).save(captor.capture());

        PatientProfile savedProfile = captor.getValue();
        assertEquals("John", savedProfile.getFirstName());
        assertEquals("Doe", savedProfile.getLastName());
        assertEquals("123", savedProfile.getOldClientGuid());
        assertEquals(200, savedProfile.getStatusId().shortValue());
    }


    @Test
    void testImportData_WithActiveClient_UpdateProfile() {
        Client client = new Client();
        client.setGuid("123");
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setStatus("ACTIVE");

        PatientProfile existingProfile = new PatientProfile();
        existingProfile.setFirstName("Jane");
        existingProfile.setLastName("Smith");
        existingProfile.setOldClientGuid("123");
        existingProfile.setStatusId((short) 210);

        when(restTemplate.getForObject(anyString(), eq(Client[].class))).thenReturn(new Client[]{client});
        when(patientProfileRepository.findByOldClientGuidContaining("123")).thenReturn(Optional.of(existingProfile));

        importService.importData();

        verify(patientProfileRepository).save(existingProfile);
        assertEquals("John", existingProfile.getFirstName());
        assertEquals("Doe", existingProfile.getLastName());
        assertEquals(200, existingProfile.getStatusId().shortValue());
    }


    @Test
    void testImportData_DeleteInactiveProfiles() {
        Client client = new Client();
        client.setGuid("123");
        client.setStatus("ACTIVE");

        PatientProfile inactiveProfile = new PatientProfile();
        inactiveProfile.setId(1L);
        inactiveProfile.setOldClientGuid("999");

        when(restTemplate.getForObject(anyString(), eq(Client[].class))).thenReturn(new Client[]{client});
        when(patientProfileRepository.findAll()).thenReturn(List.of(inactiveProfile));

        importService.importData();

        verify(patientNoteRepository).findByPatientId(1L);
        verify(patientProfileRepository).delete(inactiveProfile);
    }

    @Test
    void testImportNote_NewNote() {
        Notes note = new Notes();
        note.setComments("Test Note");
        note.setCreatedDateTime(LocalDateTime.now());
        note.setModifiedDateTime(LocalDateTime.now());

        PatientProfile profile = new PatientProfile();
        profile.setId(1L);

        when(patientNoteRepository.findByNoteAndPatientId("Test Note", 1L)).thenReturn(Optional.empty());
        when(companyUserRepository.findByLogin(anyString())).thenReturn(Optional.empty());

        importService.importNote(note, profile);

        verify(patientNoteRepository).save(any(PatientNote.class));
    }

    @Test
    void testImportNote_UpdateExistingNote() {
        Notes note = new Notes();
        note.setComments("Test Note");
        note.setCreatedDateTime(LocalDateTime.now());
        note.setModifiedDateTime(LocalDateTime.now().plusDays(1));

        PatientNote existingNote = new PatientNote();
        existingNote.setLastModifiedDateTime(LocalDateTime.now());

        PatientProfile profile = new PatientProfile();
        profile.setId(1L);

        when(patientNoteRepository.findByNoteAndPatientId("Test Note", 1L)).thenReturn(Optional.of(existingNote));
        when(companyUserRepository.findByLogin(anyString())).thenReturn(Optional.empty());

        importService.importNote(note, profile);

        verify(patientNoteRepository).save(existingNote);
        assertEquals(note.getModifiedDateTime(), existingNote.getLastModifiedDateTime());
    }

    @Test
    void testGetStatusId() {
        assertEquals((short) 200, importService.getStatusId("ACTIVE").shortValue());
        assertEquals((short) 210, importService.getStatusId("INACTIVE").shortValue());
        assertEquals((short) 230, importService.getStatusId("PENDING").shortValue());
    }

    @Test
    void testCreateCompanyUser() {
        when(companyUserRepository.save(any(CompanyUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompanyUser user = importService.createCompanyUser("test_user");

        assertEquals("test_user", user.getLogin());
        verify(companyUserRepository).save(user);
    }
}
