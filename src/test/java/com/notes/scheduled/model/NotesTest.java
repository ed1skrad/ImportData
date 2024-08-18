package com.notes.scheduled.model;

import com.notes.scheduled.model.old.Client;
import com.notes.scheduled.model.old.Notes;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotesTest {

    @Test
    void testGettersAndSetters() {
        Notes notes = new Notes();

        notes.setComments("Comments");
        notes.setGuid("guid");
        notes.setModifiedDateTime(LocalDateTime.now());
        notes.setDateTime(LocalDateTime.now());
        notes.setLoggedUser("user");
        notes.setCreatedDateTime(LocalDateTime.now());
        Client client = new Client();
        notes.setClient(client);

        assertEquals("Comments", notes.getComments());
        assertEquals("guid", notes.getGuid());
        assertEquals(notes.getModifiedDateTime(), notes.getModifiedDateTime());
        assertEquals(notes.getDateTime(), notes.getDateTime());
        assertEquals("user", notes.getLoggedUser());
        assertEquals(notes.getCreatedDateTime(), notes.getCreatedDateTime());
        assertEquals(client, notes.getClient());
    }
}

