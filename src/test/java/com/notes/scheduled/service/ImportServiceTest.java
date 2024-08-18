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
import java.util.Arrays;
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

    @Test
    void testImportClientWithNotes() {
        Client client = new Client();
        client.setGuid("guid1");
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setStatus("ACTIVE");

        Notes note1 = new Notes();
        note1.setComments("note1");
        note1.setGuid("note1-guid");
        note1.setModifiedDateTime(LocalDateTime.now());
        note1.setDateTime(LocalDateTime.now());
        note1.setLoggedUser("user1");
        note1.setCreatedDateTime(LocalDateTime.now());

        Notes note2 = new Notes();
        note2.setComments("note2");
        note2.setGuid("note2-guid");
        note2.setModifiedDateTime(LocalDateTime.now());
        note2.setDateTime(LocalDateTime.now());
        note2.setLoggedUser("user2");
        note2.setCreatedDateTime(LocalDateTime.now());

        client.setNotes(Arrays.asList(note1, note2));

        PatientProfile patientProfile = new PatientProfile();
        patientProfile.setId(1L);
        patientProfile.setFirstName("John");
        patientProfile.setLastName("Doe");
        patientProfile.setOldClientGuid("guid1");
        patientProfile.setStatusId((short) 200);

        CompanyUser companyUser1 = new CompanyUser();
        companyUser1.setLogin("user1");

        CompanyUser companyUser2 = new CompanyUser();
        companyUser2.setLogin("user2");

        when(patientProfileRepository.findByOldClientGuidContaining("guid1")).thenReturn(Optional.of(patientProfile));
        when(companyUserRepository.findByLogin("user1")).thenReturn(Optional.of(companyUser1));
        when(companyUserRepository.findByLogin("user2")).thenReturn(Optional.of(companyUser2));
        when(patientNoteRepository.findByNoteAndPatientId(anyString(), anyLong())).thenReturn(Optional.empty());

        importService.importClient(client);

        verify(patientProfileRepository, times(1)).findByOldClientGuidContaining("guid1");
        verify(companyUserRepository, times(1)).findByLogin("user1");
        verify(companyUserRepository, times(1)).findByLogin("user2");
        verify(patientNoteRepository, times(2)).findByNoteAndPatientId(anyString(), anyLong());
        verify(patientNoteRepository, times(2)).save(any(PatientNote.class));
    }
}
