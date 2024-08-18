package com.notes.scheduled.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompanyUserTest {

    @Test
    void testGettersAndSetters() {
        CompanyUser companyUser = new CompanyUser();

        companyUser.setId(1L);
        companyUser.setLogin("user");

        assertEquals(1L, companyUser.getId());
        assertEquals("user", companyUser.getLogin());
    }
}
