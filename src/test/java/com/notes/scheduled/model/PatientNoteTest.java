package com.notes.scheduled.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PatientNoteTest {

    @Test
    void testGettersAndSetters() {
        PatientNote patientNote = new PatientNote();

        patientNote.setId(1L);
        patientNote.setCreatedDateTime(LocalDateTime.now());
        patientNote.setLastModifiedDateTime(LocalDateTime.now());
        CompanyUser createdByUser = new CompanyUser();
        patientNote.setCreatedByUser(createdByUser);
        CompanyUser lastModifiedByUser = new CompanyUser();
        patientNote.setLastModifiedByUser(lastModifiedByUser);
        patientNote.setNote("Note");
        PatientProfile patient = new PatientProfile();
        patientNote.setPatient(patient);

        assertEquals(1L, patientNote.getId());
        assertEquals(patientNote.getCreatedDateTime(), patientNote.getCreatedDateTime());
        assertEquals(patientNote.getLastModifiedDateTime(), patientNote.getLastModifiedDateTime());
        assertEquals(createdByUser, patientNote.getCreatedByUser());
        assertEquals(lastModifiedByUser, patientNote.getLastModifiedByUser());
        assertEquals("Note", patientNote.getNote());
        assertEquals(patient, patientNote.getPatient());
    }
}
