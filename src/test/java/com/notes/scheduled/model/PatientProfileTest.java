package com.notes.scheduled.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PatientProfileTest {

    @Test
    void testGettersAndSetters() {
        PatientProfile patientProfile = new PatientProfile();

        patientProfile.setId(1L);
        patientProfile.setFirstName("John");
        patientProfile.setLastName("Doe");
        patientProfile.setOldClientGuid("guid");
        patientProfile.setStatusId((short) 200);

        assertEquals(1L, patientProfile.getId());
        assertEquals("John", patientProfile.getFirstName());
        assertEquals("Doe", patientProfile.getLastName());
        assertEquals("guid", patientProfile.getOldClientGuid());
        assertEquals((short) 200, patientProfile.getStatusId());
    }
}
