package com.notes.scheduled.model;

import com.notes.scheduled.model.old.Client;
import com.notes.scheduled.model.old.Notes;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientTest {

    @Test
    void testGettersAndSetters() {
        Client client = new Client();

        client.setAgency("Agency");
        client.setGuid("guid");
        client.setFirstName("John");
        client.setLastName("Doe");
        client.setStatus("ACTIVE");
        client.setDob(LocalDate.of(1990, 1, 1));
        client.setCreatedDateTime(LocalDateTime.now());
        List<Notes> notes = new ArrayList<>();
        client.setNotes(notes);

        assertEquals("Agency", client.getAgency());
        assertEquals("guid", client.getGuid());
        assertEquals("John", client.getFirstName());
        assertEquals("Doe", client.getLastName());
        assertEquals("ACTIVE", client.getStatus());
        assertEquals(LocalDate.of(1990, 1, 1), client.getDob());
        assertEquals(notes, client.getNotes());
    }
}
