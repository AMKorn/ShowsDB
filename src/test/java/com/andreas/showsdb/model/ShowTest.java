package com.andreas.showsdb.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShowTest {
    @Test
    void testCreate() {
        Show show = Show.builder()
                .id(0L)
                .name("What We Do in the Shadows")
                .country("United States")
                .build();

        assertNotNull(show);
        assertEquals(0L, show.getId());
        assertEquals("What We Do in the Shadows", show.getName());
        assertEquals("United States", show.getCountry());
    }

    @Test
    void testEqual() {
        Show show1 = Show.builder()
                .id(0L)
                .name("What We Do in the Shadows")
                .country("United States")
                .build();
        Show show2 = Show.builder()
                .id(0L)
                .name("What We Do in the Shadows")
                .country("United States")
                .build();
        Show show3 = Show.builder()
                .id(1L)
                .name("The Good Place")
                .country("United States")
                .build();
        Show show4 = Show.builder()
                .name("What We Do in the Shadows")
                .country("United States")
                .build();


        assertEquals(show1, show2);
        assertNotEquals(show1, show3);
        assertNotEquals(show1, show4);
    }
}